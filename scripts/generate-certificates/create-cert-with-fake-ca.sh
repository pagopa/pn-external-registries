#! /bin/bash

# See https://www.baeldung.com/openssl-self-signed-cert

outDir="$(dirname $0)/certs"
mkdir -p $outDir
cd $outDir
echo "outputDir=$(pwd)"

echo ""
echo ""
echo "### Create a Self-Signed Root CA"
openssl req -x509 -nodes -sha256 -days 1825 -newkey rsa:2048 -keyout rootCA.key -out rootCA.crt \
        -subj "/C=IT/L=Roma/O=PagoPA/OU=PiattaformaNotifiche/CN=FakeRootCA/"

echo ""
echo ""
echo "### Create a private key"
openssl genrsa -out domain.key 2048

echo ""
echo ""
echo "### Create Certificate Signing Request"
openssl req -subj "/C=IT/L=Roma/O=PagoPA/OU=PiattaformaNotifiche/CN=it.pagopa.pn/" \
        -nodes -key domain.key -new -out domain.csr

echo ""
echo ""
echo "### Sign the CSR with our fake Root CA"
cat > domain.ext <</EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = domain
/EOF
openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in domain.csr -out domain.crt \
             -days 365 -CAcreateserial -extfile domain.ext
