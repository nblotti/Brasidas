package ch.nblotti.brasidas.exchange.firm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
 class EODExchangeDTO {

  String code;
  String name;
  String exchange_short_name;
  String date;
  long MarketCapitalization;
  float open;
  float high;
  float low;
  float close;
  float adjusted_close;
  long volume;
  float ema_50d;
  float ema_200d;
  float hi_250d;
  float lo_250d;
  float avgvol_14d;
  float avgvol_50d;
  float avgvol_200d;

}
