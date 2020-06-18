package ch.nblotti.asset.index.to;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "index_eod_quote")
public class IndexQuoteTO {


  @Id
  @SequenceGenerator(initialValue = 2000000, name = "id_gen_index_quote", sequenceName = "index_eod_quote_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_gen_index_quote")
  private Integer id;

  @Column(name = "code")
  String code;

  @Column(name = "date")
  LocalDate date;

  @Column(name = "adjusted_close")
  float adjustedClose;

  @Column(name = "volume")
  long volume;

  public IndexQuoteTO() {

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

  public float getAdjustedClose() {
    return adjustedClose;
  }

  public void setAdjustedClose(float adjustedClose) {
    this.adjustedClose = adjustedClose;
  }

  public long getVolume() {
    return volume;
  }

  public void setVolume(long volume) {
    this.volume = volume;
  }

}
