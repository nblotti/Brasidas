package ch.nblotti.brasidas.exchange.dayoffloader;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DayOffDTO {

  private Integer id;
  private String name;
  private String code;
  private String mics;
  private String country;
  private String currency;
  private String timezone;
  private LocalDate date;
  private String holiday;


}
