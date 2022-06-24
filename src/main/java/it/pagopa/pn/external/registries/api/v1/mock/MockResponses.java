package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.external.registries.config.PnExternalRegistriesConfig;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaContactsDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaInfoDto;
import it.pagopa.pn.external.registries.generated.openapi.server.ipa.v1.dto.PaSummaryDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.AnalogDomicileDto;
import it.pagopa.pn.external.registries.generated.openapi.server.recipient.domicile.v1.dto.DigitalDomicileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MockResponses {

    public MockResponses(PnExternalRegistriesConfig pnExternalRegistriesConfig) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.findAndRegisterModules();

        this.palist = mapper.readValue( pnExternalRegistriesConfig.getMockDataResources(), new TypeReference<List<MockPa>>(){});
    }

    private List<MockPa> palist;

    private List<MockDomicilie> domiciles;

    public List<MockPa> getPalist() {
        return palist;
    }

    public void setPalist(List<MockPa> palist) {
        this.palist = palist;
    }

    public List<MockDomicilie> getDomiciles() {
        return domiciles;
    }

    public void setDomiciles(List<MockDomicilie> domiciles) {
        this.domiciles = domiciles;
    }

    public PaInfoDto getOnePa(String id) {
        MockPa pa = null;
        PaInfoDto ret;
        if (palist != null) {
            for (MockPa p: palist) {
                if (id.equals(p.getId())) {
                    pa = p;
                    break;
                }
            }
        }
        if (pa == null)
            return null;
        else {
            ret = new PaInfoDto();
            ret.setId(pa.getId());
            ret.setName(pa.getName());
            ret.setTaxId(pa.getTaxId());
            PaContactsDto pac = new PaContactsDto();
            pac.setEmail(pa.getGeneralContacts().getEmail());
            pac.setTel(pa.getGeneralContacts().getTel());
            pac.setWeb(pa.getGeneralContacts().getWeb());
            pac.setPec(pa.getGeneralContacts().getPec());
            ret.setGeneralContacts(pac);
        }
        return ret;
    }

    public List<PaSummaryDto> listOnboardedPa(String paNameFilter) {
        List<PaSummaryDto> list = new ArrayList<>();

        if (palist != null) {
            for (MockPa p: palist) {
                if (paNameFilter == null || p.getName().toLowerCase().contains(paNameFilter.toLowerCase())) {
                    PaSummaryDto pa = new PaSummaryDto();
                    pa.setId(p.getId());
                    pa.setName(p.getName());
                    list.add(pa);
                }
            }
        }
        return list;
    }

    public List<PaSummaryDto> listOnboardedPa( List<String> ids ) {
        List<PaSummaryDto> list = new ArrayList<>();

        if (palist != null) {
            for (MockPa p: palist) {
                if ( ids.contains(p.getId()) ) {
                    PaSummaryDto pa = new PaSummaryDto();
                    pa.setId(p.getId());
                    pa.setName(p.getName());
                    list.add(pa);
                }
            }
        }
        return list;
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
