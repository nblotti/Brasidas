package ch.nblotti.brasidas.exchange.split;

class EODFirmSplitDTO {

  String code;
  String exchange;
  String date;
  String split;


  public EODFirmSplitDTO() {
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

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getSplit() {
    return split;
  }

  public void setSplit(String split) {
    this.split = split;
  }
}
