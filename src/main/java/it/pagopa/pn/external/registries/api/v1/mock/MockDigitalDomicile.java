package it.pagopa.pn.external.registries.api.v1.mock;

/**
 * DigitalDomicileDto
 */

public class MockDigitalDomicile {
    private String domicileType;
    private String address;

  public String getDomicileType() {
    return domicileType;
  }

  public void setDomicileType(String domicileType) {
    this.domicileType = domicileType;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}

