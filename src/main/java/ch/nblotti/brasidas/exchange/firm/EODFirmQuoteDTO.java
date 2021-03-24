package ch.nblotti.brasidas.exchange.firm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
class EODFirmQuoteDTO {
  String date;
  float open;
  float high;
  float low;
  float close;
  float adjusted_close;
  long volume;

}
