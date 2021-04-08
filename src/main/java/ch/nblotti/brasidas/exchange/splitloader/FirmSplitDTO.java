package ch.nblotti.brasidas.exchange.splitloader;

import java.time.LocalDate;

public class FirmSplitDTO {

  private Integer id;
  String code;
  String exchange;
  LocalDate date;
  String split;
  long retry;


  public FirmSplitDTO() {
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

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getSplit() {
    return split;
  }

  public void setSplit(String split) {
    this.split = split;
  }


  public long getRetry() {
    return retry;
  }

  public void setRetry(long retry) {
    this.retry = retry;
  }
}
