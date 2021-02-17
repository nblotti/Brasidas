package ch.nblotti.brasidas.exchange.firmhighlights;

public class EODFirmHighlightsDTO {

  public long MarketCapitalization;
  public double MarketCapitalizationMln;
  public long EBITDA;
  public double PERatio;
  public double PEGRatio;
  public double WallStreetTargetPrice;
  public double BookValue;
  public double DividendShare;
  public double DividendYield;
  public double EarningsShare;
  public double EPSEstimateCurrentYear;
  public double EPSEstimateNextYear;
  public double EPSEstimateNextQuarter;
  public double EPSEstimateCurrentQuarter;
  public String MostRecentQuarter;
  public double ProfitMargin;
  public double OperatingMarginTTM;
  public double ReturnOnAssetsTTM;
  public double ReturnOnEquityTTM;
  public long RevenueTTM;
  public double RevenuePerShareTTM;
  public double QuarterlyRevenueGrowthYOY;
  public long GrossProfitTTM;
  public double DilutedEpsTTM;
  public double QuarterlyEarningsGrowthYOY;

  public EODFirmHighlightsDTO() {

  }

  public long getMarketCapitalization() {
    return MarketCapitalization;
  }

  public double getMarketCapitalizationMln() {
    return MarketCapitalizationMln;
  }

  public long getEBITDA() {
    return EBITDA;
  }

  public double getPERatio() {
    return PERatio;
  }

  public double getPEGRatio() {
    return PEGRatio;
  }

  public double getWallStreetTargetPrice() {
    return WallStreetTargetPrice;
  }

  public double getBookValue() {
    return BookValue;
  }

  public double getDividendShare() {
    return DividendShare;
  }

  public double getDividendYield() {
    return DividendYield;
  }

  public double getEarningsShare() {
    return EarningsShare;
  }

  public double getEPSEstimateCurrentYear() {
    return EPSEstimateCurrentYear;
  }

  public double getEPSEstimateNextYear() {
    return EPSEstimateNextYear;
  }

  public double getEPSEstimateNextQuarter() {
    return EPSEstimateNextQuarter;
  }

  public double getEPSEstimateCurrentQuarter() {
    return EPSEstimateCurrentQuarter;
  }

  public String getMostRecentQuarter() {
    return MostRecentQuarter;
  }

  public double getProfitMargin() {
    return ProfitMargin;
  }

  public double getOperatingMarginTTM() {
    return OperatingMarginTTM;
  }

  public double getReturnOnAssetsTTM() {
    return ReturnOnAssetsTTM;
  }

  public double getReturnOnEquityTTM() {
    return ReturnOnEquityTTM;
  }

  public long getRevenueTTM() {
    return RevenueTTM;
  }

  public double getRevenuePerShareTTM() {
    return RevenuePerShareTTM;
  }

  public double getQuarterlyRevenueGrowthYOY() {
    return QuarterlyRevenueGrowthYOY;
  }

  public long getGrossProfitTTM() {
    return GrossProfitTTM;
  }

  public double getDilutedEpsTTM() {
    return DilutedEpsTTM;
  }

  public double getQuarterlyEarningsGrowthYOY() {
    return QuarterlyEarningsGrowthYOY;
  }
}
