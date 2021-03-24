package ch.nblotti.brasidas.exchange.firmsharestats;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
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


}
