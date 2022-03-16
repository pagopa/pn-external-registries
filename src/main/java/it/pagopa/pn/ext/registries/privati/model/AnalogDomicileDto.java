package it.pagopa.pn.ext.registries.privato.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.*;


import javax.annotation.Generated;

/**
 * AnalogDomicileDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-14T15:48:37.049203200+01:00[Europe/Berlin]")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalogDomicileDto   {

  @JsonProperty("at")
  private String at;

  @JsonProperty("address")
  private String address;

  @JsonProperty("addressDetails")
  private String addressDetails;

  @JsonProperty("cap")
  private String cap;

  @JsonProperty("municipality")
  private String municipality;

  @JsonProperty("province")
  private String province;

  @JsonProperty("state")
  private String state;

  public AnalogDomicileDto at(String at) {
    this.at = at;
    return this;
  }

  /**
   * Get at
   * @return at
  */
  
  public String getAt() {
    return at;
  }

  public void setAt(String at) {
    this.at = at;
  }

  public AnalogDomicileDto address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
  */
  @NotNull 
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public AnalogDomicileDto addressDetails(String addressDetails) {
    this.addressDetails = addressDetails;
    return this;
  }

  /**
   * Get addressDetails
   * @return addressDetails
  */
  
  public String getAddressDetails() {
    return addressDetails;
  }

  public void setAddressDetails(String addressDetails) {
    this.addressDetails = addressDetails;
  }

  public AnalogDomicileDto cap(String cap) {
    this.cap = cap;
    return this;
  }

  /**
   * Get cap
   * @return cap
  */
  @NotNull 
  public String getCap() {
    return cap;
  }

  public void setCap(String cap) {
    this.cap = cap;
  }

  public AnalogDomicileDto municipality(String municipality) {
    this.municipality = municipality;
    return this;
  }

  /**
   * Get municipality
   * @return municipality
  */
  @NotNull 
  public String getMunicipality() {
    return municipality;
  }

  public void setMunicipality(String municipality) {
    this.municipality = municipality;
  }

  public AnalogDomicileDto province(String province) {
    this.province = province;
    return this;
  }

  /**
   * Get province
   * @return province
  */
  @NotNull @Size(min = 2, max = 2) 
  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public AnalogDomicileDto state(String state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
  */
  @Size(min = 2, max = 2) 
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnalogDomicileDto analogDomicile = (AnalogDomicileDto) o;
    return Objects.equals(this.at, analogDomicile.at) &&
        Objects.equals(this.address, analogDomicile.address) &&
        Objects.equals(this.addressDetails, analogDomicile.addressDetails) &&
        Objects.equals(this.cap, analogDomicile.cap) &&
        Objects.equals(this.municipality, analogDomicile.municipality) &&
        Objects.equals(this.province, analogDomicile.province) &&
        Objects.equals(this.state, analogDomicile.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(at, address, addressDetails, cap, municipality, province, state);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnalogDomicileDto {\n");
    sb.append("    at: ").append(toIndentedString(at)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    addressDetails: ").append(toIndentedString(addressDetails)).append("\n");
    sb.append("    cap: ").append(toIndentedString(cap)).append("\n");
    sb.append("    municipality: ").append(toIndentedString(municipality)).append("\n");
    sb.append("    province: ").append(toIndentedString(province)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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

