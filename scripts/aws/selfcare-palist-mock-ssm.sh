#!/usr/bin/env bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

PA_LIST=$( cat $SCRIPT_DIR/MockPaList.json | jq -c . )

PA_LIST_LEN=$(echo $PA_LIST | wc -c)

echo "PA_LIST_LEN: $PA_LIST_LEN"

if [ $PA_LIST_LEN -gt 4096 ]; then
  echo "ERROR: CloudFormation parameter max length is 4096"
  exit 1
fi

aws ssm put-parameter --profile dev --region=eu-south-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$PA_LIST"

aws ssm put-parameter --profile svil --region=eu-south-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$PA_LIST"

aws ssm put-parameter --profile coll --region=eu-south-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$PA_LIST"
