package it.pagopa.pn.external.registries.pdnd.utils;

public class AssertionGeneratorException extends Exception{

    public AssertionGeneratorException(String errorMessage){
        super(errorMessage);
    }
    public AssertionGeneratorException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
}


