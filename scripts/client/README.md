## Onboard institutions DynamoDB Refactor

Installare dipendenze node:
`npm install` 

Eseguire il comando:
`node update_all_onboardinstitutions_with_lastupdate_now.js <aws-profile>`

Dove `<aws-profile>` è il profilo dell'account AWS.

Note: 

1) lo script esegue un aggiornamento massivo della tabella `pn-OnboardInstitutions`quindi si raccomanda di eseguire un backup prima della sua esecuzione.

2) lo script viene eseguito sempre nella region `eu-south-1` 

Eseguire il comando:
`node update_onboardinstitutions_with_aoouo_columns.js <aws-profile>`

Dove `<aws-profile>` è il profilo dell'account AWS.

Note:

1) lo script esegue un aggiornamento massivo della tabella `pn-OnboardInstitutions`quindi si raccomanda di eseguire un backup prima della sua esecuzione.

2) lo script viene eseguito sempre nella region `eu-south-1` 

