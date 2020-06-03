package ch.nblotti.asset.firm.to;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Table(name = "mv_movers_volume")
@Entity
public class TopMoversTO implements Serializable {


  @Column(name = "date")
  private LocalDate date;


  @Id
  @Column(name = "id")
  private Integer id;


  @Column(name = "code")
  private String code;


  @Column(name = "exchange")
  private String exchange;

  @Column(name = "CURRENT_EXCHANGE")
  private String currentExchange;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  private String type;


  @Column(name = "volume")
  private Double volume;

  @Column(name = "isin")
  private String isin;

  @Column(name = "cusip")
  private String cusip;

  @Column(name = "updated_at")
  private LocalDate updatedat;


  @Column(name = "adjusted_close")
  private Double adjustedClose;

  @Column(name = "previous_adjusted_close")
  private Double previousAdjustedClose;

  @Column(name = "lastMove")
  private Double percentChange;


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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getVolume() {
    return volume;
  }

  public void setVolume(Double volume) {
    this.volume = volume;
  }

  public Double getPercentChange() {
    return percentChange;
  }

  public void setPercentChange(Double lastMove) {
    this.percentChange = lastMove;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public String getCusip() {
    return cusip;
  }

  public void setCusip(String cusip) {
    this.cusip = cusip;
  }

  public LocalDate getUpdatedat() {
    return updatedat;
  }

  public void setUpdatedat(LocalDate updatedat) {
    this.updatedat = updatedat;
  }

  public Double getAdjustedClose() {
    return adjustedClose;
  }

  public void setAdjustedClose(Double adjustedClose) {
    this.adjustedClose = adjustedClose;
  }

  public Double getPreviousAdjustedClose() {
    return previousAdjustedClose;
  }

  public void setPreviousAdjustedClose(Double previousAdjustedClose) {
    this.previousAdjustedClose = previousAdjustedClose;
  }

  public String getCurrentExchange() {
    return currentExchange;
  }

  public void setCurrentExchange(String currentExchange) {
    this.currentExchange = currentExchange;
  }
}
