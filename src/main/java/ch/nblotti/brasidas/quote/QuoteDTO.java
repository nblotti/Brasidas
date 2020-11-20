package ch.nblotti.brasidas.quote;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuoteDTO {


  private String date;
  private String open;
  private String high;
  private String low;
  private String close;
  @JsonProperty("adjusted_close")
  private String adjustedClose;
  private String volume;

public QuoteDTO(){

}
  public QuoteDTO(String date, String open, String high, String low, String close, String adjustedClose, String volume) {
    this.date = date;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.adjustedClose = adjustedClose;
    this.volume = volume;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getOpen() {
    return open;
  }

  public void setOpen(String open) {
    this.open = open;
  }

  public String getHigh() {
    return high;
  }

  public void setHigh(String high) {
    this.high = high;
  }

  public String getLow() {
    return low;
  }

  public void setLow(String low) {
    this.low = low;
  }

  public String getClose() {
    return close;
  }

  public void setClose(String close) {
    this.close = close;
  }

  public String getAdjustedClose() {
    return adjustedClose;
  }

  public void setAdjustedClose(String adjustedClose) {
    this.adjustedClose = adjustedClose;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

}
