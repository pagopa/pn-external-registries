package it.pagopa.pn.external.registries.exceptions;

public class AssertionGeneratorException extends PnException {

    public AssertionGeneratorException( Throwable err){
        super("Errore interno", "Errore di generazione client_assertion", 500, err);
    }
}


