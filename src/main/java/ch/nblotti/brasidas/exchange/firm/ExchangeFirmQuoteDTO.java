package ch.nblotti.brasidas.exchange.firm;

import java.time.LocalDate;

public class ExchangeFirmQuoteDTO {


  private Integer id;

  String code;

  LocalDate date;

  String name;

  String exchangeShortName;

  String actualExchange;

  long marketCapitalization;

  float adjustedClose;

  long volume;

  public ExchangeFirmQuoteDTO() {

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
