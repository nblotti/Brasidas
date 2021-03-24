package ch.nblotti.brasidas.exchange.firmhighlights;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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

}
