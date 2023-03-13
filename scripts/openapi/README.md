## PN-CODEGEN

Posizionarsi nella root del progetto ed eseguire lo script: 
`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts` 

Se si vuole eseguire il setup dello script senza l'esecuzione:

`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts -Dpagopa.codegen.noexec=true`

Se si vuole eseguire lo script sovrascrivendo la versione dell'immagine pagpa/pn-codegen da utilizzare:

`./mvnw org.apache.maven.plugins:maven-antrun-plugin:run@init-scripts -Dpagopa.codegen.version=YOUR_VERSION`

Il file aggiornato sarà presente al path `scripts/openapi/generate-code.sh` 