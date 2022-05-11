package it.pagopa.pn.external.registries.exceptions;

public class AssertionGeneratorException extends PnException {

    public AssertionGeneratorException( Throwable err){
        super("Internal error", "Error creating client_assertion", 500, err);
    }
}


