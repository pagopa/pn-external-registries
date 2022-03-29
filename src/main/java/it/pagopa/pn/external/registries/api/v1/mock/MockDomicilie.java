package it.pagopa.pn.external.registries.api.v1.mock;

/**
 * DigitalDomicileDto
 */

public class MockDomicilie {
    private String id;
    private String recipientType;
    private MockAnalogDomicile analog;
    private MockDigitalDomicile digital;

  public MockDomicilie() {
  }

  public MockAnalogDomicile getAnalog() {
    return analog;
  }

  public void setAnalog(MockAnalogDomicile analog) {
    this.analog = analog;
  }

  public MockDigitalDomicile getDigital() {
    return digital;
  }

  public void setDigital(MockDigitalDomicile digital) {
    this.digital = digital;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRecipientType() {
    return recipientType;
  }

  public void setRecipientType(String recipientType) {
    this.recipientType = recipientType;
  }
}

