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

process.env.AWS_SDK_LOAD_CONFIG=1
if(awsProfile.indexOf('sso_')>=0){ // sso profile
  credentials = new AWS.SsoCredentials({profile:awsProfile});
  AWS.config.credentials = credentials;
  AWS.config.endpoint = new AWS.Endpoint('http://localstack:4566');
} else { // IAM profile
  credentials = new AWS.SharedIniFileCredentials({profile: awsProfile});
  AWS.config.credentials = credentials;
}
AWS.config.update({region: 'eu-south-1'});


const docClient = new AWS.DynamoDB.DocumentClient();

TABLE_NAME = 'pn-OnboardInstitutions'
SCAN_LIMIT = 2000 // max 2000
DELAY_MS = 1000; //1 second

var index = 1;

const params = {
  TableName: TABLE_NAME,
  Limit: SCAN_LIMIT
};

// da utilizzare per ri-cominciare da key specifica
// in caso aggiornare anche il valore iniziale di index
/*const params = {
  TableName: TABLE_NAME,
  Limit: SCAN_LIMIT,
  ExclusiveStartKey: { iun: 'UMZL-MPEZ-GQNY-202211-M-1' }
};*/

function scanTable(params, callback) {
  docClient.scan(params, function(err, data) {
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

scanTable(params, function(err, data) {
  if (err) {
    console.log(err);
  } else {
    let now_str = new Date().toISOString();
    console.log( "Scanned items: ", data.Items.length )
    console.log( "now: ", now_str )
    data.Items.forEach(function (item) {
      const key = item.id;
      console.log("Id: ", key, "at Index: ", index++ );
      if (!item.lastUpdate) {
        const updateExpression = "SET #lastupdate = :now"
        const updateParams = {
          TableName: TABLE_NAME,
          Key: {
            "id": key
          },
          UpdateExpression: updateExpression,
          ExpressionAttributeNames: {
            "#lastupdate": 'lastUpdate'
          },
          ExpressionAttributeValues: {
            ":now": now_str
          },
          ConditionExpression: "attribute_not_exists(lastUpdate)"
        }
        docClient.update(updateParams, (err, data) => {
          if (err) {
            console.error("Errore nell'aggiornamento dell'elemento:", JSON.stringify(err, null, 2));
            console.error("Errore sull'elemento con key: ", updateParams.Key);
          } else {
            console.log("Aggiornato elemento con key: ", updateParams.Key);
          }
        })
      }
    });
  }
})
