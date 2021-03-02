package ch.nblotti.brasidas.exchange.firm;

import java.time.LocalDate;

public class FirmQuoteDTO {

  private long id;
  private LocalDate date;
  private String code;
  private float open;
  private float high;
  private float low;
  private float close;
  private float adjustedClose;
  private long volume;

  public FirmQuoteDTO() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public LocalDate getDate() {
    return date;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public float getOpen() {
    return open;
  }

  public void setOpen(float open) {
    this.open = open;
  }

  public float getHigh() {
    return high;
  }

  public void setHigh(float high) {
    this.high = high;
  }

  public float getLow() {
    return low;
  }

  public void setLow(float low) {
    this.low = low;
  }

  public float getClose() {
    return close;
  }

  public void setClose(float close) {
    this.close = close;
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

}
