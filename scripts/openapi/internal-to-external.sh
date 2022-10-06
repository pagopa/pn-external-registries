cat docs/openapi/pn-payment-info-internal-v1.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/pn-payment-info-external-v1.yaml

cat docs/openapi/pn-selfcare-internal-v1.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/pn-selfcare-external-v1.yaml