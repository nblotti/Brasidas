package ch.nblotti.brasidas.index.composition;

import java.time.LocalDate;

public class IndexCompositionDTO {

  private Integer id;

  private LocalDate date;

  private String code;

  private String exchange;

  private String name;

  private String sector;

  private String industry;

  public IndexCompositionDTO() {
  }


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSector() {
    return sector;
  }

  public void setSector(String sector) {
    this.sector = sector;
  }

  public String getIndustry() {
    return industry;
  }

  public void setIndustry(String industry) {
    this.industry = industry;
  }
}


