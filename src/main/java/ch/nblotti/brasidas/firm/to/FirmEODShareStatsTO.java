package ch.nblotti.brasidas.firm.to;


import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "FIRM_EOD_SHARE_STATS")
public class FirmEODShareStatsTO {


  @Id
  @SequenceGenerator(initialValue = 2000000, name = "firm_share_stats_id_gen_firm", sequenceName = "firm_eod_share_stats_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "firm_share_stats_id_gen_firm")
  private Integer id;


  @Column(name = "date")
  private LocalDate date;


  @Column(name = "code")
  private String code;

  @Column(name = "exchange")
  private String exchange;

  @Column(name = "shares_outstanding")
  public long sharesOutstanding;
  @Column(name = "shares_float")
  public long sharesFloat;
  @Column(name = "percent_insiders")
  public float percentInsiders;
  @Column(name = "percent_institutions")
  public float percentInstitutions;
  @Column(name = "shares_short")
  public long sharesShort;
  @Column(name = "shares_short_prior_month")
  public long sharesShortPriorMonth;
  @Column(name = "short_ratio")
  public float shortRatio;
  @Column(name = "short_percent_outstanding")
  public float shortPercentOutstanding;
  @Column(name = "short_percent_float")
  public float shortPercentFloat;


  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public long getSharesOutstanding() {
    return sharesOutstanding;
  }

  public void setSharesOutstanding(long sharesOutstanding) {
    this.sharesOutstanding = sharesOutstanding;
  }

  public long getSharesFloat() {
    return sharesFloat;
  }

  public void setSharesFloat(long sharesFloat) {
    this.sharesFloat = sharesFloat;
  }

  public float getPercentInsiders() {
    return percentInsiders;
  }

  public void setPercentInsiders(float percentInsiders) {
    this.percentInsiders = percentInsiders;
  }

  public float getPercentInstitutions() {
    return percentInstitutions;
  }

  public void setPercentInstitutions(float percentInstitutions) {
    this.percentInstitutions = percentInstitutions;
  }

  public long getSharesShort() {
    return sharesShort;
  }

  public void setSharesShort(long sharesShort) {
    this.sharesShort = sharesShort;
  }

  public long getSharesShortPriorMonth() {
    return sharesShortPriorMonth;
  }

  public void setSharesShortPriorMonth(long sharesShortPriorMonth) {
    this.sharesShortPriorMonth = sharesShortPriorMonth;
  }

  public float getShortRatio() {
    return shortRatio;
  }

  public void setShortRatio(float shortRatio) {
    this.shortRatio = shortRatio;
  }

  public float getShortPercentOutstanding() {
    return shortPercentOutstanding;
  }

  public void setShortPercentOutstanding(float shortPercentOutstanding) {
    this.shortPercentOutstanding = shortPercentOutstanding;
  }

  public float getShortPercentFloat() {
    return shortPercentFloat;
  }

  public void setShortPercentFloat(float shortPercentFloat) {
    this.shortPercentFloat = shortPercentFloat;
  }
}
