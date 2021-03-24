package ch.nblotti.brasidas.exchange.firmvaluation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
class EODValuationDTO {

  public Float TrailingPE;
  public Float ForwardPE;
  public Float PriceSalesTTM;
  public Float PriceBookMRQ;
  public Float EnterpriseValueRevenue;
  public Float EnterpriseValueEbitda;


}
