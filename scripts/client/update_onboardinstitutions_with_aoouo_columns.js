const AWS = require('aws-sdk');
/*var credentials = new AWS.SharedIniFileCredentials({profile: 'default'});
AWS.config.credentials = credentials;
AWS.config.update({region: 'us-east-1', endpoint: 'http://localhost:4566'});*/

const arguments = process.argv ;
  
if(arguments.length<=2){
  console.error("Specify AWS profile as argument")
  process.exit(1)
}

const awsProfile = arguments[2]

console.log("Using profile "+awsProfile)

let credentials = null

console.log("aws profile: ",awsProfile);

process.env.AWS_SDK_LOAD_CONFIG=1
if(awsProfile.indexOf('sso_')>=0){ // sso profile
  credentials = new AWS.SsoCredentials({profile:awsProfile});
  AWS.config.credentials = credentials;
} else { // IAM profile
  credentials = new AWS.SharedIniFileCredentials({profile: awsProfile});
  AWS.config.credentials = credentials;
}
AWS.config.update({region: 'eu-south-1'});

const docClient = new AWS.DynamoDB.DocumentClient();

TABLE_NAME = 'pn-onboardingInstitutions-copy'
SCAN_LIMIT = 2000 // max 2000
DELAY_MS = 1000; //1 second

var index = 1;

const idAooUO = new Map([["cc1c6a8e-5967-42c6-9d83-bfb12ba1665a", "7b2fff42-d3c1-44f0-b53a-bf9089a37c73"],["a95dace4-4a47-4149-a814-0e669113ce40", "16dabc75-f12e-42c4-aa0c-be9c22e9c89e"]]);


const params = {
  TableName: TABLE_NAME,
  Limit: SCAN_LIMIT
};



function scanTable(params, callback) {
  docClient.scan(params, function(err, data) {
    if (err) {
      callback(err, null);
    } else {
      setTimeout(function() {
        callback(null, data);
        
        if (typeof data.LastEvaluatedKey !== 'undefined') {
          params.ExclusiveStartKey = data.LastEvaluatedKey;
          scanTable(params, callback);
        }
      }, DELAY_MS);
    }
  });
}

scanTable(params, function(err, data) {
  if (err) {
    console.log(err);
  } else {

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
    dynamodb.updateTable(paramsDeleting, (err, data) => {
      if (err) {
        console.error('error deleting gsi:', err);
      } else {
        console.log('success deleting:', data);
      }
    });

    console.log('creating new gsi');

    const paramsCreation = {
      TableName: TABLE_NAME,
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
            IndexName: newGSIName,
            KeySchema: [
              {
                AttributeName: 'onlyRootStatus',
                KeyType: 'HASH'  
              },
              {
                AttributeName: 'lastUpdate',
                KeyType: 'RANGE' 
              }
            ]
          }
        }
      ]
    };
    console.log('creating new gsi');
    dynamodb.updateTable(paramsCreation, (err, data) => {
      if (err) {
        console.error('error creation gsi:', err);
      } else {
        console.log('succes creation:', data);
      }
    });

    console.log('start update item');
    let now_str = new Date().toISOString();
    console.log( "Scanned items: ", data.Items.length )
    console.log( "now: ", now_str )
    
    data.Items.forEach(function (item) {
      const key = item.id;
      console.log("Id: ", key, "at Index: ", index++ );
      let updateExpression = idAooUO.has(key) ?  "SET #lastupdate = :now, #rootId = :id" : "SET #lastupdate = :now, #rootId = :id, #onlyRootStatus = :status";
      let updateParams = {
        TableName: TABLE_NAME,
        Key: {
          "id": key
        },
        UpdateExpression: updateExpression,
        ExpressionAttributeNames: {
          "#lastupdate": 'lastUpdate',
          "#rootId": 'rootId',
        },
        ExpressionAttributeValues: {
          ":now": now_str,
          ":id": idAooUO.has(key) ? idAooUO.get(key) : key,
        }
      };
      if(!idAooUO.has(key)){
        updateParams.ExpressionAttributeNames["#onlyRootStatus"] = 'onlyRootStatus'
        updateParams.ExpressionAttributeValues[":status"] = "TEST"
      }

      docClient.update(updateParams, (err, data) => {
        if (err) {
          console.error("Errore nell'aggiornamento dell'elemento:", JSON.stringify(err, null, 2));
          console.error("Errore sull'elemento con key: ", updateParams.Key);
        } else {
          console.log("Aggiornato elemento con key: ", updateParams.Key);
        }
      })
  });
  }
})
