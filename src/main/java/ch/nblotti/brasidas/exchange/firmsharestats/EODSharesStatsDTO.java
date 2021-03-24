package ch.nblotti.brasidas.exchange.firmsharestats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
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

}
