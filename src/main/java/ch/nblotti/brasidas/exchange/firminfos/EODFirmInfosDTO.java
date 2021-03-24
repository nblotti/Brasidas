package ch.nblotti.brasidas.exchange.firminfos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EODFirmInfosDTO {

  String Code;
  String Type;
  String Name;
  String Exchange;
  String CurrencyCode;
  String CurrencyName;
  String CurrencySymbol;
  String CountryName;
  String CountryISO;
  String ISIN;
  String CUSIP;
  String CIK;
  String EmployerIdNumber;
  String FiscalYearEnd;
  String IPODate;
  String InternationalDomestic;
  String Sector;
  String Industry;
  String GicSector;
  String GicGroup;
  String GicIndustry;
  String GicSubIndustry;
  String HomeCategory;
  Boolean IsDelisted;
  String Description;
  String Address;
  String Phone;
  String WebURL;
  String LogoURL;
  Integer FullTimeEmployees;
  String UpdatedAt;

}
