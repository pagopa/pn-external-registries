package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * AnalogDomicileDto
 */

public class MockAnalogDomicile {

  private String at;

  private String address;

  private String addressDetails;

  private String cap;

  private String municipality;

  private String province;

  private String state;

  public MockAnalogDomicile() {
  }

  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddressDetails() {
    return addressDetails;
  }

  public void setAddressDetails(String addressDetails) {
    this.addressDetails = addressDetails;
  }

  public String getCap() {
    return cap;
  }

  public void setCap(String cap) {
    this.cap = cap;
  }

  public String getMunicipality() {
    return municipality;
  }

  public void setMunicipality(String municipality) {
    this.municipality = municipality;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

}

