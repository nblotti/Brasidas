package ch.nblotti.asset.index.to;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "FIRM_EOD_HIGHLIGHTS")
public class FirmEODHighlightsTO {


  @Id
  @SequenceGenerator(initialValue = 2000000, name = "firm_highlights_id_gen_firm", sequenceName = "firm_eod_highlights_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "firm_highlights_id_gen_firm")
  private Integer id;


  @Column(name = "date")
  private LocalDate date;

  @Column(name = "code")
  private String code;

  @Column(name = "exchange")
  private String exchange;

  @Column(name = "market_capitalization")
  public long marketCapitalization;
  @Column(name = "market_capitalization_mln")
  public double marketCapitalizationMln;
  @Column(name = "ebitda")
  public long eBITDA;
  @Column(name = "pe_ratio")
  public double pERatio;
  @Column(name = "peg_ratio")
  public double pEGRatio;
  @Column(name = "wall_street_target_price")
  public double wallStreetTargetPrice;
  @Column(name = "book_value")
  public double bookValue;
  @Column(name = "dividend_share")
  public double dividendShare;
  @Column(name = "dividend_yield")
  public double dividendYield;
  @Column(name = "earning_share")
  public double earningsShare;
  @Column(name = "eps_estimate_current_year")
  public double ePSEstimateCurrentYear;
  @Column(name = "eps_estimate_next_year")
  public double ePSEstimateNextYear;
  @Column(name = "eps_estimate_next_quarter")
  public double ePSEstimateNextQuarter;
  @Column(name = "eps_estimate_current_quarter")
  public double ePSEstimateCurrentQuarter;
  @Column(name = "most_recent_quarter")
  public String mostRecentQuarter;
  @Column(name = "profit_margin")
  public double profitMargin;
  @Column(name = "operting_margin_ttm")
  public double operatingMarginTTM;
  @Column(name = "return_on_assets_ttm")
  public double returnOnAssetsTTM;
  @Column(name = "return_on_equity_ttm")
  public double returnOnEquityTTM;
  @Column(name = "revenue_ttm")
  public long revenueTTM;
  @Column(name = "revenue_per_share_ttm")
  public double revenuePerShareTTM;
  @Column(name = "quarter_revenue_growth_yoy")
  public double quarterlyRevenueGrowthYOY;
  @Column(name = "gross_profit_ttm")
  public long grossProfitTTM;
  @Column(name = "diluted_eps_ttm")
  public double dilutedEpsTTM;
  @Column(name = "quarterly_earning_growth_yoy")
  public double quarterlyEarningsGrowthYOY;

  public FirmEODHighlightsTO() {

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
