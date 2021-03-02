package ch.nblotti.brasidas.exchange.firm;

class EODFirmQuoteDTO {
  String date;
  float open;
  float high;
  float low;
  float close;
  float adjusted_close;
  long volume;


  public EODFirmQuoteDTO() {
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
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

  public float getAdjusted_close() {
    return adjusted_close;
  }

  public void setAdjusted_close(float adjusted_close) {
    this.adjusted_close = adjusted_close;
  }

  public long getVolume() {
    return volume;
  }

  public void setVolume(long volume) {
    this.volume = volume;
  }

}
