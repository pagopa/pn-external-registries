package it.pagopa.pn.ext.registries.privati.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import javax.validation.constraints.*;


import javax.annotation.Generated;

/**
 * DigitalDomicileDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-14T15:48:37.049203200+01:00[Europe/Berlin]")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DigitalDomicileDto   {

  /**
   * Gets or Sets domicileType
   */
  public enum DomicileTypeEnum {
    PEC("PEC"),
    
    IO("IO");

    private String value;

    DomicileTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DomicileTypeEnum fromValue(String value) {
      for (DomicileTypeEnum b : DomicileTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("domicileType")
  private DomicileTypeEnum domicileType;

  @JsonProperty("address")
  private String address;

  public DigitalDomicileDto domicileType(DomicileTypeEnum domicileType) {
    this.domicileType = domicileType;
    return this;
  }

  /**
   * Get domicileType
   * @return domicileType
  */
  @NotNull 
  public DomicileTypeEnum getDomicileType() {
    return domicileType;
  }

  public void setDomicileType(DomicileTypeEnum domicileType) {
    this.domicileType = domicileType;
  }

  public DigitalDomicileDto address(String address) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DigitalDomicileDto digitalDomicile = (DigitalDomicileDto) o;
    return Objects.equals(this.domicileType, digitalDomicile.domicileType) &&
        Objects.equals(this.address, digitalDomicile.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(domicileType, address);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DigitalDomicileDto {\n");
    sb.append("    domicileType: ").append(toIndentedString(domicileType)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

