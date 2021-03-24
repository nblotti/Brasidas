package ch.nblotti.brasidas.exchange.firmvaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class FirmValuationDTO {


  private Integer id;


  private LocalDate date;

  private String code;

  private String exchange;

  public float TrailingPE;
  public float ForwardPE;
  public float PriceSalesTTM;
  public float PriceBookMRQ;
  public float EnterpriseValueRevenue;
  public float EnterpriseValueEbitda;

}
