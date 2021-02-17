package ch.nblotti.brasidas.exchange.firmsharestats;


import java.time.LocalDate;

public class FirmShareStatsDTO {


  private Integer id;


  private LocalDate date;


  private String code;

  private String exchange;

  public long sharesOutstanding;
  public long sharesFloat;
  public float percentInsiders;
  public float percentInstitutions;
  public long sharesShort;
  public long sharesShortPriorMonth;
  public float shortRatio;
  public float shortPercentOutstanding;
  public float shortPercentFloat;


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

  public long getSharesOutstanding() {
    return sharesOutstanding;
  }

  public void setSharesOutstanding(long sharesOutstanding) {
    this.sharesOutstanding = sharesOutstanding;
  }

  public long getSharesFloat() {
    return sharesFloat;
  }

  public void setSharesFloat(long sharesFloat) {
    this.sharesFloat = sharesFloat;
  }

  public float getPercentInsiders() {
    return percentInsiders;
  }

  public void setPercentInsiders(float percentInsiders) {
    this.percentInsiders = percentInsiders;
  }

  public float getPercentInstitutions() {
    return percentInstitutions;
  }

  public void setPercentInstitutions(float percentInstitutions) {
    this.percentInstitutions = percentInstitutions;
  }

  public long getSharesShort() {
    return sharesShort;
  }

  public void setSharesShort(long sharesShort) {
    this.sharesShort = sharesShort;
  }

  public long getSharesShortPriorMonth() {
    return sharesShortPriorMonth;
  }

  public void setSharesShortPriorMonth(long sharesShortPriorMonth) {
    this.sharesShortPriorMonth = sharesShortPriorMonth;
  }

  public float getShortRatio() {
    return shortRatio;
  }

  public void setShortRatio(float shortRatio) {
    this.shortRatio = shortRatio;
  }

  public float getShortPercentOutstanding() {
    return shortPercentOutstanding;
  }

  public void setShortPercentOutstanding(float shortPercentOutstanding) {
    this.shortPercentOutstanding = shortPercentOutstanding;
  }

  public float getShortPercentFloat() {
    return shortPercentFloat;
  }

  public void setShortPercentFloat(float shortPercentFloat) {
    this.shortPercentFloat = shortPercentFloat;
  }
}
