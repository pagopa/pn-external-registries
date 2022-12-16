Quando serve aggiornare le liste, basta aggiungere un nuovo item
con data di __creazione__ E __update__ __più recente__ dell'ultimo inserito.
Si può ricaricare tranquillamente tutto dato che verranno semplicemente sovrascritti
i dati.

Per caricare i dati in localstack usare il comando:

export PROFILE="default" ; export REGION="us-east-1" ; export ENV="localstack" ; export ENDPOINT="http://localstack:4566" ; ./init-pa-list.sh

Per caricare i dati in DEV:

export PROFILE="dev" ; export REGION="us-east-1" ; export ENV="dev" ; export ENDPOINT="http://dynamodb.eu-south-1.amazonaws.com" ; ./init-pa-list.sh