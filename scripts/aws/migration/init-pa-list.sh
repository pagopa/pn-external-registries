aws dynamodb batch-write-item \
	  --profile $PROFILE --region $REGION  --endpoint-url=$ENDPOINT --request-items file://init-pa-list-$ENV.json