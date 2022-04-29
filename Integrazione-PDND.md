# PN-EXTERNAL-REGISTRIES - Integrazione PDND


PDND è la piattaforma che gestisce i servizi di directory, di autorizzazione e di autenticazione 
per l’accesso ai servizi offerti dalla dalle piattaforme digitali della pubblica amministrazione.

Le amministrazioni che desiderano __rendere disponibile un servizio__:
- si registrano alla piattaforma PDND
- definiscono le caratteristiche del servizio
- le interfacce esposte
- la modalità di fruizione
- le policy di autenticazione

Le amministrazioni che desiderano __usufruire di un servizio__ esposto da una pubblica amministrazione:
- si registrano sulla piattaforma PDND
- fanno richiesta di accesso ai servizi esposti dalle altre amministrazioni
- dopo aver ottenuto l’autorizzazione all’accesso possono iniziare ad utilizzare i servizi richiesti.

## Accesso alla piattaforma in modalità M2M

Un sistema che desidera usufruire delle funzionalità offerte della piattaforma PDND deve accedere 
all’interfaccia web ed eseguire la registrazione come fruitore oppure come erogatore di servizio.

PDND espone dei servizi attraverso i quali un sistema può accedere a PDND per richiedere l’accesso 
di un servizio esposto da un’amministrazione oppure per registrare un servizio che si intende esporre.

## Configurazione di un client che desidera accedere alla piattaforma interop M2M

Per configurare un client, che accede alla piattaforma PDND per utilizzare l’interfaccia M2M, 
bisogna connettersi all’interfaccia con profilo “admin”, selezionare la voce del menu verticale 
a sinistra “Fruizione” quindi “Interop M2M”.

La pagina visualizza due tab: “Dettagli” e “Client associati”.

Premendo “Specifica OpenAPI” il sistema scarica la descrizione dell’interfaccia REST esposta dal modulo, 
premendo “Client associati” è possibile definire un nuovo client.

Per definire un nuovo client, selezionare “Client associati” e poi “+Aggiungi”. Si apre quindi una pagina dove è 
necessario inserire il nome del client, la descrizione del client e la lista degli operatori di sicurezza abilitati 
a caricare la chiave di sicurezza del nuovo client.

Collegarsi quindi al sistema usando la matricola di uno degli operatori di sicurezza che sono stati inseriti nella 
configurazione del client.

Dal menu laterale selezionare “Fruizione” quindi “Interop M2M” e quindi “Client associati”. 
Appare la lista dei client. Selezionare il tasto “Ispeziona” che si trova a fianco di ogni client.

Si apre una pagina con tre tab “Client assertion”, “Operatori di sicurezza” e “Chiavi pubbliche”.

Selezionare il tab “Chiavi pubbliche” e premere il tasto “Aggiungi”.

Dopo aver premuto il tasto “Aggiungi” si apre una form all’interno della quale inserire il nome della chiave e la 
chiave pubblica.

Nota: la pagina contiene un help in linea che spiega come generare la chiave.

## Accesso M2M a PDND

Il client che accede ai servizi offerti dalla piattaforma PDND tramite interfaccia M2M deve autenticarsi per 
accedere ai servizi.

L’autenticazione richiede che vengano specificati alcuni parametri:

- Client_assertion
- Client_asserion_type
- Grant_type
- Client_ID 

Alcuni di questi parametri sono ricavabili dall’interfaccia web di PDND. 

Collegarsi a PDND con uno dei profili (admin o security) e dal menu a sinistra selezionare “Fruizione” -> 
“Interop M2M” -> “Client associati”. Nella lista selezionare il client che interessa, di cui si è in possesso
della chiave privata, e premere il tasto “Ispeziona”. La pressione del tasto porta alla pagina “Client assertion” 
dove sono riportati i parametri per costruire la client assertion. 

Copiare i parametri:

- ID Client
- Audience 

e salvare la chiave pubblica che è possibile scaricare premendo “Chiavi pubbliche”, “Ispeziona” e poi premendo il 
bottone “Scarica”.

La costruzione del “Client_assertion” è specificata nel paragrafo seguente:

- Il parametro client_assertion_type deve valere `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`
- Il paramtro grant_type deve valere `client_credentials`
- Il parametro client_id è il valore del campo “ID Client” ricavato dall’interfaccia.

## Come generare la client assertion per ottenere un token usabile su InteropM2M

Le informazioni di seguito denominate come "kid", "clientId" e "audience" sono reperibili sulla Piattaforma 
Interoperabilità per lo specifico client e chiave che si stanno usando.

L'header prevede tre campi: "kid", "alg" e "typ":

- kid: l'id della chiave. La chiave può essere scelta tra quelle disponibili nel pool di chiavi per un determinato 
 client, che verrà usato nel payload come cliendId
- alg: l'algoritmo utilizzato per firmare questo JWT (come JWS). In questo momento il valore è sempre "RS256"
- typ: il tipo di oggetto che sto inviando, sempre "JWT" 

Il payload prevede sei campi: "iss", "sub", "aud", "jti", "iat" e "exp":

- iss: l'issuer, in questo caso il clientId
- sub: il subject, in questo caso sempre il clientId
- aud: l'audience, disponibile sulla Piattaforma Interoperabilità
- jti: il JWT ID, un id unico (uuid) random assegnato da chi vuole creare il token, serve per tracciare il token 
  stesso. Deve essere cura del chiamante assicurarsi che l'id di questo token sia unico
- iat: l'issued at, il timestamp riportante data e ora in cui viene creato il token, espresso in UNIX epoch (valore numerico, non stringa)
- exp: l'expiration, il timestamp riportante data e ora di scadenza del token, espresso in UNIX epoch  (valore numerico, non stringa)

## Fruizione di un E-Service

Un client che desidera usufruire dei servizi esposti da un E-Service deve accedere a PDND e richiedere da PDND un 
token per accedere a E-Service desiderato.

Uno stesso token può essere usato per diverse transazioni nel rispetto dei vincoli temporali di validità del token 
definiti dall’erogatore.

## Profili degli utenti

Gli utenti con ruolo “Rappresentante Legale” oppure con ruolo “Delegato” e profilo “admin” possono creare i client 
ma non possono inserire le chiavi pubbliche dei client.

Gli utenti con il ruolo di “Operatore Sicurezza” e profilo di interop/security sono abilitati a caricare nel sistema 
le chiavi pubbliche dei client all’interno della sezione “Interop M2M” -> “Client associati”, selezionato “ISPEZIONA” 
accanto all’identificativo di un client, premendo sulla voce “Chiavi pubbliche” possono aggiungere le chiavi pubbliche
di un client. 

Questo profilo non è abilitato alla creazione di nuovi client.

#Guida alla client assertion

Sulla Piattaforma Interoperabilità, è possibile fare due tipi diversi di client assertion. 
La prima prevede lo stacco di un token da spendere direttamente sull'API machine-to-machine della piattaforma 
(InteropM2M), utile per fare operazioni sulla Piattaforma Interoperabilità senza passare dalla UI. 

La seconda permette di staccare un token firmato dalla Piattaforma Interoperabilità che sarà spendibile sull'E-Service 
dell'ente erogatore presso il quale, in qualità di fruitore, avete una richiesta di fruizione attiva.

 
In principio sono simili, ma hanno alcune differenze, che vediamo nel dettaglio.

- Cos'è una client assertion?
- Come generare la client assertion per ottenere un token usabile su InteropM2M?
- Come generare la client assertion per ottenere un token usabile su un E-Service?


### Cos'è una client assertion?

È un tipo di client credential del flusso OAuth. Per la specifica si rimanda all'RFC. 

È un meccanismo attraverso il quale riesco ad autenticarmi presso un server autorizzativo per ottenere un token da 
spendere presso il possessore di una risorsa che mi interessa, che richiede autorizzazione per l'accesso. 

Un volta ottenuto il token firmato dalla Piattaforma Interoperabilità, questo sarà spendibile presso l'API 
machine-to-machine della Piattaforma stessa. Il token dovrà essere inserito come "Bearer" nell'header "Authorization".

### Come generare la client assertion per ottenere un token usabile su un E-Service?

La client assertion per staccare un token da spendere presso l'E-Service di un ente erogatore deve contenere gli 
stessi campi indicati per la client assertion InteropM2M, con una differenza nel payload:
- __purposeId__: un campo in più che identifica la finalità per la quale si sta facendo la richiesta, reperibile sulla 
  Piattaforma Interoperabilità 

Una volta ottenuto un token firmato dalla Piattaforma Interoperabilità, questo sarà spendibile sull'E-Service 
dell'erogatore secondo i termini e le audience stabilite nella descrizione dell'E-Service stesso, nella richiesta di 
fruizione, e nella finalità indicata dal fruitore.

## Generazione delle chiavi di autenticazione dei client

```bash
openssl genrsa -out client-test-keypair.rsa.pem 2048
openssl rsa -in client-test-keypair.rsa.pem -pubout -out client-test-keypair.rsa.pub
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in client-test-keypair.rsa.pem -out client-test-keypair.rsa.priv
```
