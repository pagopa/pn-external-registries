
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
[
   {
      "id":"9115f90b-1dc9-4ba8-8645-b38bda016b8f",
      "name":"Unione Dei Comuni del Golfo Paradiso",
      "fiscalCode":"90067990102",
      "mailAddress":"protocollo@pec.unionecomunigolfoparadiso.ge.it",
      "status":"ACTIVE",
      "address":"Via Stagno, 19"
   },
   {
      "id":"7ac8d531-9c46-48eb-965a-25c12fa1fd81",
      "name":"Istituto Nazionale Previdenza Sociale - INPS",
      "fiscalCode":"80078750587",
      "mailAddress":"ufficiosegreteria.direttoregenerale@postacert.inps.gov.it",
      "status":"ACTIVE",
      "address":"Via Ciro Il Grande, 21"
   },
   {
      "id":"4db741cf-17e1-4751-9b7b-7675ccca472b",
      "name":"Agenzia delle Entrate",
      "fiscalCode":"06363391001",
      "mailAddress":"agenziaentratepec@pce.agenziaentrate.it",
      "status":"ACTIVE",
      "address":"Via Giorgione 106"
   },
   {
      "id":"5fc82440-0a04-401e-b1f9-b1e9af2429ab",
      "name":"Comune di Castelnuovo Magra",
      "fiscalCode":"00115020117",
      "mailAddress":"protocollo@pec.castelnuovomagra.com",
      "status":"ACTIVE",
      "address":"Via Canale"
   },
   {
      "id":"cc1c6a8e-5967-42c6-9d83-bfb12ba1665a",
      "name":"Comune di Firenze",
      "fiscalCode":"01307110484",
      "mailAddress":"protocollo@pec.comune.fi.it",
      "status":"ACTIVE",
      "address":"Palazzo Vecchio, Piazza Della Signoria"
   },
   {
      "id":"ef29949d-1167-4af9-86f4-23bcaaf6e41b",
      "name":"Agenzia delle Entrate - Riscossione",
      "fiscalCode":"13756881002",
      "mailAddress":"protocollo@pec.agenziariscossione.gov.it",
      "status":"ACTIVE",
      "address":"Via Giuseppe Grezar,14"
   },
   {
      "id":"1962d21c-c701-4805-93f6-53a877898756",
      "name":"PagoPA S.p.A.",
      "fiscalCode":"15376371009",
      "mailAddress":"selfcare@pec.pagopa.it",
      "status":"ACTIVE",
      "address":"Piazza Colonna, 370"
   },
   {
      "id":"b6c5b42a-8a07-436f-96ce-8c2ab7f4dbd2",
      "name":"Comune di Valsamoggia",
      "fiscalCode":"03334231200",
      "mailAddress":"comune.valsamoggia@cert.provincia.bo.it",
      "status":"ACTIVE",
      "address":"Piazza Garibaldi 1 loc. Bazzano"
   },
   {
      "id":"88d30379-9249-443d-82f5-084fb7a81daf",
      "name":"Istituto Nazionale per l'Assicurazione contro gli Infortuni sul Lavoro - INAIL",
      "fiscalCode":"01165400589",
      "mailAddress":"presidenza@postacert.inail.it",
      "status":"ACTIVE",
      "address":"Via IV Novembre 144"
   },
   {
      "id":"79e9ab1c-2f25-4dc9-8e59-843f7dc8c5a7",
      "name":"Comune di Figline e Incisa Valdarno",
      "fiscalCode":"06396970482",
      "mailAddress":"comune.figlineincisa@postacert.toscana.it",
      "status":"ACTIVE",
      "address":"Piazza Del Municipio, 5"
   },
   {
      "id":"16dabc75-f12e-42c4-aa0c-be9c22e9c89e",
      "name":"Comune di Agrigento",
      "fiscalCode":"00074260845",
      "mailAddress":"servizio.protocollo@pec.comune.agrigento.it",
      "status":"ACTIVE",
      "address":"Piazza Pirandello, 35"
   },
   {
      "id":"5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce",
      "name":"Comune di Palermo",
      "fiscalCode":"80016350821",
      "mailAddress":"protocollo@cert.comune.palermo.it",
      "status":"ACTIVE",
      "address":"Piazza Pretoria, 1 - Palazzo Delle Aquile"
   },
   {
      "id":"026e8c72-7944-4dcd-8668-f596447fec6d",
      "name":"Comune di Milano",
      "fiscalCode":"01199250158",
      "mailAddress":"protocollo@postacert.comune.milano.it",
      "status":"ACTIVE",
      "address":"Piazza Della Scala, 2"
   },
   {
      "id":"a95dace4-4a47-4149-a814-0e669113ce40",
      "name":"Comune di Verona",
      "fiscalCode":"00215150236",
      "mailAddress":"protocollo.informatico@pec.comune.verona.it",
      "status":"ACTIVE",
      "address":"Piazza Bra, 1"
   }
]
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
