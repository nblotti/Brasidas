package ch.nblotti.brasidas.exchange.firm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
public class ExchangeFirmQuoteDTO {


  private Integer id;

  String code;

  LocalDate date;

  String name;

  String exchangeShortName;

  String actualExchange;

  long marketCapitalization;

  float adjustedClose;

  long volume;

}
