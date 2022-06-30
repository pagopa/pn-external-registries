
### DEV ###
MOCK_DEV=$(cat <<-END
[{"id":"d0d28367-1695-4c50-a260-6fda526e9aab","name":"Comune di Milano","taxId":"01199250158","generalContacts":{"pec":"protocollo@postacert.comune.milano.it","email":"protocollo@comune.milano.it","tel":"023456789","web":"www.comune.milano.it"}}]
END
)

aws ssm put-parameter --profile dev --region=eu-central-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$MOCK_DEV"

### UAT/SVIL/COLL ###
MOCK_UAT=$(cat <<-END
[{"id":"026e8c72-7944-4dcd-8668-f596447fec6d","name":"Comune di Milano","taxId":"01199250158","generalContacts":{"pec":"protocollo@postacert.comune.milano.it","email":"protocollo@comune.milano.it","tel":"023456789","web":"www.comune.milano.it"}}]
END
)

aws ssm put-parameter --profile uat --region=eu-central-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$MOCK_UAT"

aws ssm put-parameter --profile svil --region=eu-south-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$MOCK_UAT"

aws ssm put-parameter --profile coll --region=eu-south-1 --no-cli-pager \
--name "MockPaList" --type "String" --overwrite \
--description "Mock della lista della PA di Self-care" \
--value "$MOCK_UAT"
