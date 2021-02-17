package ch.nblotti.brasidas.exchange.firm;

 class EODExchangeDTO {

  String code;
  String name;
  String exchange_short_name;
  String date;
  long MarketCapitalization;
  float open;
  float high;
  float low;
  float close;
  float adjusted_close;
  long volume;
  float ema_50d;
  float ema_200d;
  float hi_250d;
  float lo_250d;
  float avgvol_14d;
  float avgvol_50d;
  float avgvol_200d;


  public EODExchangeDTO() {
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExchange_short_name() {
    return exchange_short_name;
  }

  public void setExchange_short_name(String exchange_short_name) {
    this.exchange_short_name = exchange_short_name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public long getMarketCapitalization() {
    return MarketCapitalization;
  }

  public void setMarketCapitalization(long marketCapitalization) {
    MarketCapitalization = marketCapitalization;
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

  public float getEma_50d() {
    return ema_50d;
  }

  public void setEma_50d(float ema_50d) {
    this.ema_50d = ema_50d;
  }

  public float getEma_200d() {
    return ema_200d;
  }

  public void setEma_200d(float ema_200d) {
    this.ema_200d = ema_200d;
  }

  public float getHi_250d() {
    return hi_250d;
  }

  public void setHi_250d(float hi_250d) {
    this.hi_250d = hi_250d;
  }

  public float getLo_250d() {
    return lo_250d;
  }

  public void setLo_250d(float lo_250d) {
    this.lo_250d = lo_250d;
  }

  public float getAvgvol_14d() {
    return avgvol_14d;
  }

  public void setAvgvol_14d(float avgvol_14d) {
    this.avgvol_14d = avgvol_14d;
  }

  public float getAvgvol_50d() {
    return avgvol_50d;
  }

  public void setAvgvol_50d(float avgvol_50d) {
    this.avgvol_50d = avgvol_50d;
  }

  public float getAvgvol_200d() {
    return avgvol_200d;
  }

  public void setAvgvol_200d(float avgvol_200d) {
    this.avgvol_200d = avgvol_200d;
  }
}
