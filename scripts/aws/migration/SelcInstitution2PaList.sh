JQ_EXPR="{\"pn-OnboardInstitutions\": .items | map(. |
  { \"PutRequest\": {
    \"Item\": {
      \"id\": {
        \"S\": .id
      },
      \"description\": {
        \"S\": .description
      },
      \"taxCode\": {
        \"S\": .taxCode
      },
      \"digitalAddress\": {
        \"S\": .digitalAddress
      },
      \"status\": {
        \"S\": \"ACTIVE\"
      },
      \"created\": {
        \"S\": \"2022-12-12T11:51:43.777996900Z\"
      },
      \"lastUpdate\": {
        \"S\": \"2022-12-12T11:51:43.777996900Z\"
      },
      \"address\": {
        \"S\": .address
      }
    }
  }
 })
}"

jq "$JQ_EXPR" prod-pn-coll.json >init-pa-list-coll.json
