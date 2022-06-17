package it.pagopa.pn.external.registries.api.v1.mock;

import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MockResponses {


    private List<MockDomicilie> domiciles;



    public List<MockDomicilie> getDomiciles() {
        return domiciles;
    }

    public void setDomiciles(List<MockDomicilie> domiciles) {
        this.domiciles = domiciles;
    }

    public AnalogDomicileDto getOneAnalogDomicile(String recipientType, String opaqueId) {
        AnalogDomicileDto ret = null;
        if (domiciles != null) {
            for (MockDomicilie md: domiciles) {
                if (md.getId().equals(opaqueId) &&
                    md.getRecipientType().equals(recipientType) && md.getAnalog() != null) {

                    ret = new AnalogDomicileDto();

                    ret.setAddress(md.getAnalog().getAddress());
                    ret.setCap(md.getAnalog().getCap());
                    ret.setMunicipality(md.getAnalog().getMunicipality());
                    ret.setProvince(md.getAnalog().getProvince());
                    ret.setState(md.getAnalog().getState());
                    ret.setAt(md.getAnalog().getAt());
                    ret.setAddressDetails(md.getAnalog().getAddressDetails());

                    break;
                }
            }
        }

        return ret;
    }

    public DigitalDomicileDto getOneDigitalDomicile(String recipientType, String opaqueId) {
        DigitalDomicileDto ret = null;

        if (domiciles != null) {
            for (MockDomicilie md: domiciles) {
                if (md.getId().equals(opaqueId) &&
                        md.getRecipientType().equals(recipientType) && md.getDigital() != null) {

                    ret = new DigitalDomicileDto();

                    ret.setAddress(md.getDigital().getAddress());
                    if (md.getDigital().getDomicileType().equals(DigitalDomicileDto.DomicileTypeEnum.PEC.name()))
                        ret.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.PEC);
                    else
                        ret.setDomicileType(DigitalDomicileDto.DomicileTypeEnum.IO);

                    break;
                }
            }
        }

        return ret;
    }
}
