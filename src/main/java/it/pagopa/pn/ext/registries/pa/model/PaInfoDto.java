package it.pagopa.pn.ext.registries.pa.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;


import javax.annotation.Generated;

/**
 * Denomination, fiscal code, pec, email, tel, www site, ...
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-14T15:48:36.815856800+01:00[Europe/Berlin]")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaInfoDto   {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("taxId")
  private String taxId;

  @JsonProperty("generalContacts")
  private PaContactsDto generalContacts;

  public PaInfoDto id(String id) {
    this.id = id;
    return this;
  }

  /**
   * An unique ID that identify a Public Administration
   * @return id
  */
  @NotNull @Size(min = 1) 
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public PaInfoDto name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  @NotNull @Size(min = 1) 
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PaInfoDto taxId(String taxId) {
    this.taxId = taxId;
    return this;
  }

  /**
   * codice fiscale
   * @return taxId
  */
  @NotNull @Size(min = 1) 
  public String getTaxId() {
    return taxId;
  }

  public void setTaxId(String taxId) {
    this.taxId = taxId;
  }

  public PaInfoDto generalContacts(PaContactsDto generalContacts) {
    this.generalContacts = generalContacts;
    return this;
  }

  /**
   * Get generalContacts
   * @return generalContacts
  */
  @NotNull @Valid 
  public PaContactsDto getGeneralContacts() {
    return generalContacts;
  }

  public void setGeneralContacts(PaContactsDto generalContacts) {
    this.generalContacts = generalContacts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaInfoDto paInfo = (PaInfoDto) o;
    return Objects.equals(this.id, paInfo.id) &&
        Objects.equals(this.name, paInfo.name) &&
        Objects.equals(this.taxId, paInfo.taxId) &&
        Objects.equals(this.generalContacts, paInfo.generalContacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, taxId, generalContacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaInfoDto {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    taxId: ").append(toIndentedString(taxId)).append("\n");
    sb.append("    generalContacts: ").append(toIndentedString(generalContacts)).append("\n");
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

