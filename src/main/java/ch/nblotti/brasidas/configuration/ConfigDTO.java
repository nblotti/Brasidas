package ch.nblotti.brasidas.configuration;

public class ConfigDTO {

  private Integer id;

  private String code;

  private String type;


  private String value;


  public ConfigDTO() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
