package ch.nblotti.asset.index.to;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "firm_eod_valuation")
public class FirmEODValuationTO {


  @Id
  @SequenceGenerator(initialValue = 2000000, name = "firm_valuation_id_gen_firm", sequenceName = "firm_eod_valuation_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "firm_valuation_id_gen_firm")
  private Integer id;


  @Column(name = "date")
  private LocalDate date;

  @Column(name = "code")
  private String code;

  @Column(name = "exchange")
  private String exchange;

  @Column(name = "trailing_pe")
  public float TrailingPE;
  @Column(name = "forward_pe")
  public float ForwardPE;
  @Column(name = "price_sales_ttm")
  public float PriceSalesTTM;
  @Column(name = "price_book_mrq")
  public float PriceBookMRQ;
  @Column(name = "enterprise_value_revenue")
  public float EnterpriseValueRevenue;
  @Column(name = "enterprise_value_ebitda")
  public float EnterpriseValueEbitda;

  public FirmEODValuationTO(){

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
