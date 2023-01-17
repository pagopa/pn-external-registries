Quando serve aggiornare le liste, basta aggiungere un nuovo item
con data di __creazione__ E __update__ __più recente__ dell'ultimo inserito.
Si può ricaricare tranquillamente tutto dato che verranno semplicemente sovrascritti
i dati.

Per caricare i dati in localstack usare il comando:

export PROFILE="default" ; export REGION="us-east-1" ; export ENV="localstack" ; export ENDPOINT="http://localstack:4566" ; ./init-pa-list.sh

Per caricare i dati in DEV:

```
export PROFILE="dev"
export REGION="us-east-1"
export ENV="dev" 
export ENDPOINT="http://dynamodb.eu-south-1.amazonaws.com"
./init-pa-list.sh
```

Ottenuto il file institutions _prod-pn-coll.json_ da SelfCare è possibile trasformarlo in un json
d'inserimento dei dati sulla tabella _pn-OnboardInstitutions_ tramite lo script _SelcInstitution2PaList.sh_

Purtroppo il numero di batch-write-item è limitato, quindi se il file è troppo grande deve essere spezzato.
```
export PROFILE="coll"
export REGION="eu-south-1"
export ENV="coll" 
export ENDPOINT="http://dynamodb.eu-south-1.amazonaws.com"
aws dynamodb batch-write-item \
	  --profile $PROFILE --region $REGION  --endpoint-url=$ENDPOINT --request-items file://init-pa-list-$ENV-1.json
aws dynamodb batch-write-item \
	  --profile $PROFILE --region $REGION  --endpoint-url=$ENDPOINT --request-items file://init-pa-list-$ENV-2.json	  
```

