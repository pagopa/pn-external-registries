## PN-CODEGEN

### Quickstart
Posizionarsi nella root del progetto ed eseguire lo script: 
`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts` 

Successivamente eseguire:
`./scripts/openapi/generate-code.sh`

### Parametri
Se si vuole anche eseguire lo script oltre che generarlo:

`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts -Dpagopa.codegen.exec=true`

Se si vuole eseguire lo script senza generarne una nuova copia:

`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts -Dpagopa.codegen.nocopy=true`

### Personalizzazione tag
Nota: è possibile anche fornire un tag di pn-codegen tramite argomento, ad es.:
`./scripts/openapi/generate-code.sh v01.00.01`