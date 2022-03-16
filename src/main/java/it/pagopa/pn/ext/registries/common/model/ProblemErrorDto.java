package it.pagopa.pn.ext.registries.common.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.*;


import javax.annotation.Generated;

/**
 * ProblemErrorDto
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-03-14T15:48:36.815856800+01:00[Europe/Berlin]")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemErrorDto   {

  @JsonProperty("code")
  private String code;

  @JsonProperty("element")
  private String element;

  @JsonProperty("detail")
  private String detail;

  public ProblemErrorDto code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Internal code of the error
   * @return code
  */
  @NotNull @Pattern(regexp = "^[0-9]{3}-[0-9]{4}$") @Size(min = 8, max = 8) 
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public ProblemErrorDto element(String element) {
    this.element = element;
    return this;
  }

  /**
   * Parameter or request body field name for validation error
   * @return element
  */
  
  public String getElement() {
    return element;
  }

  public void setElement(String element) {
    this.element = element;
  }

  public ProblemErrorDto detail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * A human readable explanation specific to this occurrence of the problem.
   * @return detail
  */
  @NotNull @Size(max = 1024) 
  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProblemErrorDto problemError = (ProblemErrorDto) o;
    return Objects.equals(this.code, problemError.code) &&
        Objects.equals(this.element, problemError.element) &&
        Objects.equals(this.detail, problemError.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, element, detail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProblemErrorDto {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    element: ").append(toIndentedString(element)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
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

