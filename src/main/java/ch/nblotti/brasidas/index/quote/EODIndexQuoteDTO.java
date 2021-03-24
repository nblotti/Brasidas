package ch.nblotti.brasidas.index.quote;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
class EODIndexQuoteDTO {
  String code;
  String date;
  float open;
  float high;
  float low;
  float close;
  float adjusted_close;
  long volume;


}
