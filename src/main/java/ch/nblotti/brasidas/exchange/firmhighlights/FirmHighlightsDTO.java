package ch.nblotti.brasidas.exchange.firmhighlights;

import java.time.LocalDate;

public class FirmHighlightsDTO {


  private Integer id;


  private LocalDate date;

  private String code;

  private String exchange;

  public long marketCapitalization;
  public double marketCapitalizationMln;
  public long eBITDA;
  public double pERatio;
  public double pEGRatio;
  public double wallStreetTargetPrice;
  public double bookValue;
  public double dividendShare;
  public double dividendYield;
  public double earningsShare;
  public double ePSEstimateCurrentYear;
  public double ePSEstimateNextYear;
  public double ePSEstimateNextQuarter;
  public double ePSEstimateCurrentQuarter;
  public String mostRecentQuarter;
  public double profitMargin;
  public double operatingMarginTTM;
  public double returnOnAssetsTTM;
  public double returnOnEquityTTM;
  public long revenueTTM;
  public double revenuePerShareTTM;
  public double quarterlyRevenueGrowthYOY;
  public long grossProfitTTM;
  public double dilutedEpsTTM;
  public double quarterlyEarningsGrowthYOY;

  public FirmHighlightsDTO() {

  }

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

  public long getMarketCapitalization() {
    return marketCapitalization;
  }

  public void setMarketCapitalization(long marketCapitalization) {
    this.marketCapitalization = marketCapitalization;
  }

  public double getMarketCapitalizationMln() {
    return marketCapitalizationMln;
  }

  public void setMarketCapitalizationMln(double marketCapitalizationMln) {
    this.marketCapitalizationMln = marketCapitalizationMln;
  }

  public long geteBITDA() {
    return eBITDA;
  }

  public void seteBITDA(long eBITDA) {
    this.eBITDA = eBITDA;
  }

  public double getpERatio() {
    return pERatio;
  }

  public void setpERatio(double pERatio) {
    this.pERatio = pERatio;
  }

  public double getpEGRatio() {
    return pEGRatio;
  }

  public void setpEGRatio(double pEGRatio) {
    this.pEGRatio = pEGRatio;
  }

  public double getWallStreetTargetPrice() {
    return wallStreetTargetPrice;
  }

  public void setWallStreetTargetPrice(double wallStreetTargetPrice) {
    this.wallStreetTargetPrice = wallStreetTargetPrice;
  }

  public double getBookValue() {
    return bookValue;
  }

  public void setBookValue(double bookValue) {
    this.bookValue = bookValue;
  }

  public double getDividendShare() {
    return dividendShare;
  }

  public void setDividendShare(double dividendShare) {
    this.dividendShare = dividendShare;
  }

  public double getDividendYield() {
    return dividendYield;
  }

  public void setDividendYield(double dividendYield) {
    this.dividendYield = dividendYield;
  }

  public double getEarningsShare() {
    return earningsShare;
  }

  public void setEarningsShare(double earningsShare) {
    this.earningsShare = earningsShare;
  }

  public double getePSEstimateCurrentYear() {
    return ePSEstimateCurrentYear;
  }

  public void setePSEstimateCurrentYear(double ePSEstimateCurrentYear) {
    this.ePSEstimateCurrentYear = ePSEstimateCurrentYear;
  }

  public double getePSEstimateNextYear() {
    return ePSEstimateNextYear;
  }

  public void setePSEstimateNextYear(double ePSEstimateNextYear) {
    this.ePSEstimateNextYear = ePSEstimateNextYear;
  }

  public double getePSEstimateNextQuarter() {
    return ePSEstimateNextQuarter;
  }

  public void setePSEstimateNextQuarter(double ePSEstimateNextQuarter) {
    this.ePSEstimateNextQuarter = ePSEstimateNextQuarter;
  }

  public double getePSEstimateCurrentQuarter() {
    return ePSEstimateCurrentQuarter;
  }

  public void setePSEstimateCurrentQuarter(double ePSEstimateCurrentQuarter) {
    this.ePSEstimateCurrentQuarter = ePSEstimateCurrentQuarter;
  }

  public String getMostRecentQuarter() {
    return mostRecentQuarter;
  }

  public void setMostRecentQuarter(String mostRecentQuarter) {
    this.mostRecentQuarter = mostRecentQuarter;
  }

  public double getProfitMargin() {
    return profitMargin;
  }

  public void setProfitMargin(double profitMargin) {
    this.profitMargin = profitMargin;
  }

  public double getOperatingMarginTTM() {
    return operatingMarginTTM;
  }

  public void setOperatingMarginTTM(double operatingMarginTTM) {
    this.operatingMarginTTM = operatingMarginTTM;
  }

  public double getReturnOnAssetsTTM() {
    return returnOnAssetsTTM;
  }

  public void setReturnOnAssetsTTM(double returnOnAssetsTTM) {
    this.returnOnAssetsTTM = returnOnAssetsTTM;
  }

  public double getReturnOnEquityTTM() {
    return returnOnEquityTTM;
  }

  public void setReturnOnEquityTTM(double returnOnEquityTTM) {
    this.returnOnEquityTTM = returnOnEquityTTM;
  }

  public long getRevenueTTM() {
    return revenueTTM;
  }

  public void setRevenueTTM(long revenueTTM) {
    this.revenueTTM = revenueTTM;
  }

  public double getRevenuePerShareTTM() {
    return revenuePerShareTTM;
  }

  public void setRevenuePerShareTTM(double revenuePerShareTTM) {
    this.revenuePerShareTTM = revenuePerShareTTM;
  }

  public double getQuarterlyRevenueGrowthYOY() {
    return quarterlyRevenueGrowthYOY;
  }

  public void setQuarterlyRevenueGrowthYOY(double quarterlyRevenueGrowthYOY) {
    this.quarterlyRevenueGrowthYOY = quarterlyRevenueGrowthYOY;
  }

  public long getGrossProfitTTM() {
    return grossProfitTTM;
  }

  public void setGrossProfitTTM(long grossProfitTTM) {
    this.grossProfitTTM = grossProfitTTM;
  }

  public double getDilutedEpsTTM() {
    return dilutedEpsTTM;
  }

  public void setDilutedEpsTTM(double dilutedEpsTTM) {
    this.dilutedEpsTTM = dilutedEpsTTM;
  }

  public double getQuarterlyEarningsGrowthYOY() {
    return quarterlyEarningsGrowthYOY;
  }

  public void setQuarterlyEarningsGrowthYOY(double quarterlyEarningsGrowthYOY) {
    this.quarterlyEarningsGrowthYOY = quarterlyEarningsGrowthYOY;
  }

}
