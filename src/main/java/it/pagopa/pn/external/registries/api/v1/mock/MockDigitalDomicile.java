package it.pagopa.pn.external.registries.api.v1.mock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DigitalDomicileDto
 */

public class MockDigitalDomicile {
    private String DomicileType;
    private String address;

  public MockDigitalDomicile() {
  }

  public String getDomicileType() {
    return DomicileType;
  }

  public void setDomicileType(String domicileType) {
    DomicileType = domicileType;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}

