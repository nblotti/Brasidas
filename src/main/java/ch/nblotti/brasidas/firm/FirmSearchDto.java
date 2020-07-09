package ch.nblotti.brasidas.firm;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FirmSearchDto {

  public String cusip;
  public String code;
  @JsonProperty("current_exchange")
  public String currentExchange;
  @JsonProperty("gic_sub_industry")
  public String gicSubIndustry;
  public String description;
  public String industry;
  @JsonProperty("gic_group")
  public String gicGroup;
  public String type;
  public String name;
  @JsonProperty("gic_sector")
  public String gicSector;
  public String exchange;
  @JsonProperty("gic_industry")
  public String gicIndustry;
  public String id;
  public String sector;
  public String isin;

  @JsonProperty("_version_")
  public Long version;


  public FirmSearchDto() {
  }

  public String getCusip() {
    return cusip;
  }

  public void setCusip(String cusip) {
    this.cusip = cusip;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getGicSubIndustry() {
    return gicSubIndustry;
  }

  public void setGicSubIndustry(String gicSubIndustry) {
    this.gicSubIndustry = gicSubIndustry;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIndustry() {
    return industry;
  }

  public void setIndustry(String industry) {
    this.industry = industry;
  }

  public String getGicGroup() {
    return gicGroup;
  }

  public void setGicGroup(String gicGroup) {
    this.gicGroup = gicGroup;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGicSector() {
    return gicSector;
  }

  public void setGicSector(String gicSector) {
    this.gicSector = gicSector;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getCurrentExchange() {
    return currentExchange;
  }

  public void setCurrentExchange(String currentExchange) {
    this.currentExchange = currentExchange;
  }

  public String getGicIndustry() {
    return gicIndustry;
  }

  public void setGicIndustry(String gicIndustry) {
    this.gicIndustry = gicIndustry;
  }

  public String getSector() {
    return sector;
  }

  public void setSector(String sector) {
    this.sector = sector;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
