package ch.nblotti.brasidas.exchange.firmhighlights;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
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


}
