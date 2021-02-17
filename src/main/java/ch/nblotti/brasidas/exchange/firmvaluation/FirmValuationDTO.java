package ch.nblotti.brasidas.exchange.firmvaluation;

import java.time.LocalDate;

public class FirmValuationDTO {


  private Integer id;


  private LocalDate date;

  private String code;

  private String exchange;

  public float TrailingPE;
  public float ForwardPE;
  public float PriceSalesTTM;
  public float PriceBookMRQ;
  public float EnterpriseValueRevenue;
  public float EnterpriseValueEbitda;

  public FirmValuationDTO() {

  }

  public Integer getId() {
    return id;
  }

  public LocalDate getDate() {
    return date;
  }

  public String getCode() {
    return code;
  }

  public String getExchange() {
    return exchange;
  }

  public float getTrailingPE() {
    return TrailingPE;
  }

  public float getForwardPE() {
    return ForwardPE;
  }

  public float getPriceSalesTTM() {
    return PriceSalesTTM;
  }

  public float getPriceBookMRQ() {
    return PriceBookMRQ;
  }

  public float getEnterpriseValueRevenue() {
    return EnterpriseValueRevenue;
  }

  public float getEnterpriseValueEbitda() {
    return EnterpriseValueEbitda;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public void setTrailingPE(float trailingPE) {
    TrailingPE = trailingPE;
  }

  public void setForwardPE(float forwardPE) {
    ForwardPE = forwardPE;
  }

  public void setPriceSalesTTM(float priceSalesTTM) {
    PriceSalesTTM = priceSalesTTM;
  }

  public void setPriceBookMRQ(float priceBookMRQ) {
    PriceBookMRQ = priceBookMRQ;
  }

  public void setEnterpriseValueRevenue(float enterpriseValueRevenue) {
    EnterpriseValueRevenue = enterpriseValueRevenue;
  }

  public void setEnterpriseValueEbitda(float enterpriseValueEbitda) {
    EnterpriseValueEbitda = enterpriseValueEbitda;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }
}
