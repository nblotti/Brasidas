package ch.nblotti.asset.firm.to;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "firm_eod_quote")
public class FirmEODQuoteTO {


  @Id
  @SequenceGenerator(initialValue = 2000000, name = "id_gen_firm_quote", sequenceName = "firm_eod_quote_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen_firm_quote")
  private Integer id;

  @Column(name = "code")
  String code;

  @Column(name = "date")
  LocalDate date;

  @Column(name = "name")
  String name;

  @Column(name = "exchange")
  String exchangeShortName;

  @Transient
  String actualExchange;

  @Column(name = "market_cap")
  long marketCapitalization;

  @Column(name = "adjusted_close")
  float adjustedClose;

  @Column(name = "volume")
  long volume;

  public FirmEODQuoteTO() {

  }


  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExchangeShortName() {
    return exchangeShortName;
  }

  public void setExchangeShortName(String exchangeShortName) {
    this.exchangeShortName = exchangeShortName;
  }

  public long getMarketCapitalization() {
    return marketCapitalization;
  }

  public void setMarketCapitalization(long marketCapitalization) {
    this.marketCapitalization = marketCapitalization;
  }

  public float getAdjustedClose() {
    return adjustedClose;
  }

  public void setAdjustedClose(float adjustedClose) {
    this.adjustedClose = adjustedClose;
  }

  public long getVolume() {
    return volume;
  }

  public void setVolume(long volume) {
    this.volume = volume;
  }

  public String getActualExchange() {
    return actualExchange;
  }

  public void setActualExchange(String actualExchange) {
    this.actualExchange = actualExchange;
  }
}
