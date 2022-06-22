package it.pagopa.pn.external.registries.api.v1.mock;

import java.net.URI;

public class MockPaContacts {

  private String pec;

  private String email;

  private String tel;

  private URI web;

  public String getPec() {
    return pec;
  }

  public MockPaContacts() {
  }

  public void setPec(String pec) {
    this.pec = pec;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setTel(String tel) {
    this.tel = tel;
  }

  public void setWeb(URI web) {
    this.web = web;
  }

  public String getEmail() {
    return email;
  }

  public String getTel() {
    return tel;
  }

  public URI getWeb() {
    return web;
  }


}

