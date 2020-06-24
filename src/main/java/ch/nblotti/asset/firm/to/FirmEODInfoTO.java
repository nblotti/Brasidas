package ch.nblotti.asset.firm.to;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "firm_eod_info")
public class FirmEODInfoTO {

  @Id
  @SequenceGenerator(initialValue = 2000000, name = "id_gen_firm_info", sequenceName = "firm_eod_info_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen_firm_info")
  private Integer id;

  @Column(name = "code")
  String code;

  @Column(name = "date")
  LocalDate date;

  @Column(name = "type")
  String type;
  @Column(name = "name")
  String name;
  @Column(name = "exchange")
  String exchange;
  @Column(name = "current_exchange")
  String currentExchange;
  @Column(name = "currency_code")
  String currencyCode;
  @Column(name = "currency_name")
  String currencyName;
  @Column(name = "currency_symbol")
  String currencySymbol;
  @Column(name = "country_name")
  String countryName;
  @Column(name = "country_iso")
  String countryISO;
  @Column(name = "isin")
  String isin;
  @Column(name = "cusip")
  String cusip;
  @Column(name = "cik")
  String cik;
  @Column(name = "employer_id_number")
  String employerIdNumber;
  @Column(name = "fiscal_year_end")
  String fiscalYearEnd;
  @Column(name = "ipo_date")
  String iPODate;
  @Column(name = "international_domestic")
  String internationalDomestic;
  @Column(name = "sector")
  String sector;
  @Column(name = "industry")
  String industry;
  @Column(name = "gic_sector")
  String gicSector;
  @Column(name = "gic_group")
  String gicGroup;
  @Column(name = "gic_industry")
  String gicIndustry;
  @Column(name = "gic_sub_industry")
  String gicSubIndustry;
  @Column(name = "description")
  String description;
  @Column(name = "address")
  String address;
  @Column(name = "phone")
  String phone;
  @Column(name = "web_url")
  String webURL;
  @Column(name = "logo_url")
  String logoURL;
  @Column(name = "full_time_employees")
  Integer fullTimeEmployees;
  @Column(name = "updated_at")
  String updatedAt;


  public FirmEODInfoTO() {

  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getCurrencyName() {
    return currencyName;
  }

  public void setCurrencyName(String currencyName) {
    this.currencyName = currencyName;
  }

  public String getCurrencySymbol() {
    return currencySymbol;
  }

  public void setCurrencySymbol(String currencySymbol) {
    this.currencySymbol = currencySymbol;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getCountryISO() {
    return countryISO;
  }

  public void setCountryISO(String countryISO) {
    this.countryISO = countryISO;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public String getcCusip() {
    return cusip;
  }

  public void setcCusip(String cUSIP) {
    this.cusip = cUSIP;
  }

  public String getcCik() {
    return cik;
  }

  public void setcCik(String cIK) {
    this.cik = cIK;
  }

  public String getEmployerIdNumber() {
    return employerIdNumber;
  }

  public void setEmployerIdNumber(String employerIdNumber) {
    this.employerIdNumber = employerIdNumber;
  }

  public String getFiscalYearEnd() {
    return fiscalYearEnd;
  }

  public void setFiscalYearEnd(String fiscalYearEnd) {
    this.fiscalYearEnd = fiscalYearEnd;
  }

  public String getiPODate() {
    return iPODate;
  }

  public void setiPODate(String iPODate) {
    this.iPODate = iPODate;
  }

  public String getInternationalDomestic() {
    return internationalDomestic;
  }

  public void setInternationalDomestic(String internationalDomestic) {
    this.internationalDomestic = internationalDomestic;
  }

  public String getSector() {
    return sector;
  }

  public void setSector(String sector) {
    this.sector = sector;
  }

  public String getIndustry() {
    return industry;
  }

  public void setIndustry(String industry) {
    this.industry = industry;
  }

  public String getGicSector() {
    return gicSector;
  }

  public void setGicSector(String gicSector) {
    this.gicSector = gicSector;
  }

  public String getGicGroup() {
    return gicGroup;
  }

  public void setGicGroup(String gicGroup) {
    this.gicGroup = gicGroup;
  }

  public String getGicIndustry() {
    return gicIndustry;
  }

  public void setGicIndustry(String gicIndustry) {
    this.gicIndustry = gicIndustry;
  }

  public String getGicSubIndustry() {
    return gicSubIndustry;
  }

  public void setGicSubIndustry(String gicSubIndustry) {
    this.gicSubIndustry = gicSubIndustry;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getWebURL() {
    return webURL;
  }

  public void setWebURL(String webURL) {
    this.webURL = webURL;
  }

  public String getLogoURL() {
    return logoURL;
  }

  public void setLogoURL(String logoURL) {
    this.logoURL = logoURL;
  }

  public Integer getFullTimeEmployees() {
    return fullTimeEmployees;
  }

  public void setFullTimeEmployees(Integer fullTimeEmployees) {
    this.fullTimeEmployees = fullTimeEmployees;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCurrentExchange() {
    return currentExchange;
  }

  public void setCurrentExchange(String currentExchange) {
    this.currentExchange = currentExchange;
  }

  public String getCusip() {
    return cusip;
  }

  public void setCusip(String cusip) {
    this.cusip = cusip;
  }

  public String getCik() {
    return cik;
  }

  public void setCik(String cik) {
    this.cik = cik;
  }
}
