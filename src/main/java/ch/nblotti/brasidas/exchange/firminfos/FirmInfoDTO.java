package ch.nblotti.brasidas.exchange.firminfos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@NoArgsConstructor
@Getter
@Setter
public class FirmInfoDTO {

  private Integer id;

  String code;

  LocalDate date;

  String type;
  String name;
  String exchange;
  String currentExchange;
  String currencyCode;
  String currencyName;
  String currencySymbol;
  String countryName;
  String countryISO;
  String isin;
  String cusip;
  String cik;
  String employerIdNumber;
  String fiscalYearEnd;
  String iPODate;
  String internationalDomestic;
  String sector;
  String industry;
  String gicSector;
  String gicGroup;
  String gicIndustry;
  String gicSubIndustry;
  String description;
  String address;
  String phone;
  String webURL;
  String logoURL;
  Integer fullTimeEmployees;
  String updatedAt;


}
