package it.pagopa.pn.external.registries.pdnd.dto;

public class TokenDto {
    private HeaderTokenDto headerTokenDto;
    private PayloadTokenDto payloadTokenDto;

    private String porpouseId="NOT-INITIALIZED";

    public TokenDto(String porpouseId){
        this.porpouseId=porpouseId;
    }
    public void add(String header,String payload){

    }
    public String getBearerToken(){
        return porpouseId;
    }
}
