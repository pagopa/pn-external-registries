package it.pagopa.pn.ext.registries.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;


import javax.annotation.Generated;

/**
 * pec, email, tel, www site, ...
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-14T15:48:36.815856800+01:00[Europe/Berlin]")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaContactsDto   {

  @JsonProperty("pec")
  private String pec;

  @JsonProperty("email")
  private String email;

  @JsonProperty("tel")
  private String tel;

  @JsonProperty("web")
  private URI web;

  public PaContactsDto pec(String pec) {
    this.pec = pec;
    return this;
  }

  /**
   * Get pec
   * @return pec
  */
  @Size(min = 1) 
  public String getPec() {
    return pec;
  }

  public void setPec(String pec) {
    this.pec = pec;
  }

  public PaContactsDto email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  @Size(min = 1) 
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public PaContactsDto tel(String tel) {
    this.tel = tel;
    return this;
  }

  /**
   * Get tel
   * @return tel
  */
  @Pattern(regexp = "^\\\\+?([0-9]|-)*") @Size(min = 1, max = 20) 
  public String getTel() {
    return tel;
  }

  public void setTel(String tel) {
    this.tel = tel;
  }

  public PaContactsDto web(URI web) {
    this.web = web;
    return this;
  }

  /**
   * Get web
   * @return web
  */
  @Valid @Pattern(regexp = "http.*") @Size(min = 1) 
  public URI getWeb() {
    return web;
  }

  public void setWeb(URI web) {
    this.web = web;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaContactsDto paContacts = (PaContactsDto) o;
    return Objects.equals(this.pec, paContacts.pec) &&
        Objects.equals(this.email, paContacts.email) &&
        Objects.equals(this.tel, paContacts.tel) &&
        Objects.equals(this.web, paContacts.web);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pec, email, tel, web);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaContactsDto {\n");
    sb.append("    pec: ").append(toIndentedString(pec)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    tel: ").append(toIndentedString(tel)).append("\n");
    sb.append("    web: ").append(toIndentedString(web)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

