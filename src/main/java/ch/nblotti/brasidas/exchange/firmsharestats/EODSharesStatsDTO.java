package ch.nblotti.brasidas.exchange.firmsharestats;

 class EODSharesStatsDTO {
  public long SharesOutstanding;
  public long SharesFloat;
  public float PercentInsiders;
  public float PercentInstitutions;
  public long SharesShort;
  public long SharesShortPriorMonth;
  public float ShortRatio;
  public float ShortPercentOutstanding;
  public float ShortPercentFloat;

  public EODSharesStatsDTO() {
  }

  public long getSharesOutstanding() {
    return SharesOutstanding;
  }

  public void setSharesOutstanding(long sharesOutstanding) {
    SharesOutstanding = sharesOutstanding;
  }

  public long getSharesFloat() {
    return SharesFloat;
  }

  public void setSharesFloat(long sharesFloat) {
    SharesFloat = sharesFloat;
  }

  public float getPercentInsiders() {
    return PercentInsiders;
  }

  public void setPercentInsiders(float percentInsiders) {
    PercentInsiders = percentInsiders;
  }

  public float getPercentInstitutions() {
    return PercentInstitutions;
  }

  public void setPercentInstitutions(float percentInstitutions) {
    PercentInstitutions = percentInstitutions;
  }

  public long getSharesShort() {
    return SharesShort;
  }

  public void setSharesShort(long sharesShort) {
    SharesShort = sharesShort;
  }

  public long getSharesShortPriorMonth() {
    return SharesShortPriorMonth;
  }

  public void setSharesShortPriorMonth(long sharesShortPriorMonth) {
    SharesShortPriorMonth = sharesShortPriorMonth;
  }

  public float getShortRatio() {
    return ShortRatio;
  }

  public void setShortRatio(float shortRatio) {
    ShortRatio = shortRatio;
  }

  public float getShortPercentOutstanding() {
    return ShortPercentOutstanding;
  }

  public void setShortPercentOutstanding(float shortPercentOutstanding) {
    ShortPercentOutstanding = shortPercentOutstanding;
  }

  public float getShortPercentFloat() {
    return ShortPercentFloat;
  }

  public void setShortPercentFloat(float shortPercentFloat) {
    ShortPercentFloat = shortPercentFloat;
  }
}
