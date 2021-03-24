package ch.nblotti.brasidas.exchange.firm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class FirmQuoteDTO {

  private long id;
  private LocalDate date;
  private String code;
  private float open;
  private float high;
  private float low;
  private float close;
  private float adjustedClose;
  private long volume;


}
