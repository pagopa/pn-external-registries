const { DynamoDBClient, ScanCommand, UpdateTableCommand, UpdateItemCommand } = require("@aws-sdk/client-dynamodb");
const { marshall } = require("@aws-sdk/util-dynamodb");
const { fromIni } = require("@aws-sdk/credential-provider-ini");
const { STSClient, AssumeRoleCommand } = require("@aws-sdk/client-sts");

const arguments = process.argv;

if (arguments.length <= 2) {
  console.error("Specify AWS profile as argument");
  process.exit(1);
}

const awsProfile = arguments[2]
const roleArn = arguments[3]

console.log("Using profile " + awsProfile);

function awsProfileConfig() {
  if(awsProfile.indexOf('sso_')>=0){
    return { 
      region: "eu-south-1", 
      credentials: fromIni({ 
        profile: awsProfile,
      })
    }
  }else{
    return { 
      region: "eu-south-1", 
      credentials: fromIni({ 
        profile: awsProfile,
        roleAssumer: async (sourceCredentials, params) => {
          const stsClient = new STSClient({ credentials: sourceCredentials });
          const command = new AssumeRoleCommand({
            RoleArn: roleArn,
            RoleSessionName: "session1"
          });
          const response = await stsClient.send(command);
          return {
            accessKeyId: response.Credentials.AccessKeyId,
            secretAccessKey: response.Credentials.SecretAccessKey,
            sessionToken: response.Credentials.SessionToken,
            expiration: response.Credentials.Expiration
          };
        }
      })
    }
  }
}

const dynamoDBClient = new DynamoDBClient(awsProfileConfig());
console.log("DOCUMENT CLIENT CREATO");

const TABLE_NAME = 'pn-onboardingInstitutions-copy';
const SCAN_LIMIT = 10; // max 2000
DELAY_MS = 1000; //1 second

var index = 1;

const idAooUO = new Map([
  /* EXAMPLE VALUE
  ["cc1c6a8e-5967-42c6-9d83-bfb12ba1665a", "16dabc75-f12e-42c4-aa0c-be9c22e9c89e"],
  ["a95dace4-4a47-4149-a814-0e669113ce40", "16dabc75-f12e-42c4-aa0c-be9c22e9c89e"],
  ["7b2fff42-d3c1-44f0-b53a-bf9089a37c73", "16dabc75-f12e-42c4-aa0c-be9c22e9c89e"]
  */
]);

const scanParams = {
  TableName: TABLE_NAME,
  Limit: SCAN_LIMIT
};


function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}


async function scanTable(params, callback) {
  const scanCommand = new ScanCommand(params);
  dynamoDBClient.send(scanCommand, function(err, data) {
    if (err) {
      callback(err, null);
    } else {
      setTimeout(function() {
        callback(null, data);

        if (typeof data.LastEvaluatedKey !== 'undefined') {
          params.ExclusiveStartKey = data.LastEvaluatedKey;
          //console.log("Params with LEK: ", params)
          scanTable(params, callback);
        }
      }, DELAY_MS);
    }
  });
}


async function main(){
  console.log('update gsi');

  const paramsDeleting = {
    TableName: TABLE_NAME,
    GlobalSecondaryIndexUpdates: [
      {
        Delete: {
          IndexName: 'status-lastUpdate-gsi'
        }
      }
    ]
  };

  console.log('deleting old gsi');
  const deleteGsiCommand = new UpdateTableCommand(paramsDeleting);
    try {
      await dynamoDBClient.send(deleteGsiCommand);
      console.log("success deleting gsi");
    } catch (error) {
      console.log("error deleting gsi");
      throw error;
    }

    console.log('Start sleep 10s');
    await sleep(10000);
    console.log('Start sleep 10s');
    
  const paramsCreation = {
    TableName: TABLE_NAME,
    BillingMode: "PAY_PER_REQUEST",
    AttributeDefinitions: [
      {
        AttributeName: 'onlyRootStatus',
        AttributeType: 'S'
      },
      {
        AttributeName: 'lastUpdate',
        AttributeType: 'S'
      }
    ],
    GlobalSecondaryIndexUpdates: [
      {
        Create: {
          IndexName: 'status-lastUpdate-gsi',
          KeySchema: [
            {
              AttributeName: 'onlyRootStatus',
              KeyType: 'HASH'
            },
            {
              AttributeName: 'lastUpdate',
              KeyType: 'RANGE'
            }
          ],
          Projection: {
            ProjectionType: 'ALL'
          }
        }
      }
    ]
  };
  console.log('starting creation new gsi');
  const createGsiCommand = new UpdateTableCommand(paramsCreation);
  try {
    await dynamoDBClient.send(createGsiCommand);
    console.log("success creating gsi");
  } catch (error) {
    console.log("error creating gsi");
    throw error;
  }
  

  console.log('start update item');

  scanTable(scanParams, function(err, data) {
    if (err) {
      console.log(err);
    } else {

      let now_str = new Date().toISOString();
      console.log("Scanned items: ", data.length);
      console.log("now: ", now_str);
  
      data.Items.forEach(async function (item) {
      const key = item.id.S;
      console.log("Id: ", key, "at Index: ", index++);
      let updateExpression = idAooUO.has(key) ? "SET #lastupdate = :now, #rootId = :id" : "SET #lastupdate = :now, #rootId = :id, #onlyRootStatus = :status";
      let updateParams = {
        TableName: TABLE_NAME,
        Key: {
            "id": { S: key }
        },
        UpdateExpression: updateExpression,
        ExpressionAttributeNames: {
          "#lastupdate": 'lastUpdate',
          "#rootId": 'rootId',
        },
        ExpressionAttributeValues: marshall(idAooUO.has(key) ?
          {
            ":now": now_str,
            ":id": idAooUO.get(key),
          } 
          :
          {
            ":now": now_str,
            ":id": key,
            ":status": "ACTIVE"
          } 
          )
        };
      if (!idAooUO.has(key)) {
        updateParams.ExpressionAttributeNames["#onlyRootStatus"] = 'onlyRootStatus';
      }
  
      try {
        const updateItemCommand = new UpdateItemCommand(updateParams);
        await dynamoDBClient.send(updateItemCommand);
          console.log("Aggiornato elemento con key: ", key);
      } catch (error) {
        console.error("Errore nell'aggiornamento dell'elemento:", JSON.stringify(error, null, 2));
        console.error("Errore sull'elemento con key: ", key);
      }
    });
    }
  })
      
}

main();



