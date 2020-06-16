package ch.nblotti.asset.index.to;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Table(name = "mv_movers_index")
@Entity
public class IndexMoversTO implements Serializable {


  @Column(name = "date")
  private LocalDate date;


  @Id
  @Column(name = "row_number")
  private Integer id;

  @Column(name = "code")
  private String code;

  @Column(name = "type")
  private int nbrDays;


  @Column(name = "volume")
  private Double volume;

  @Column(name = "adjusted_close")
  private Double adjustedClose;

  @Column(name = "previous_adjusted_close")
  private Double previousAdjustedClose;

  @Column(name = "lastMove")
  private Double percentChange;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

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

  public Double getVolume() {
    return volume;
  }

  public void setVolume(Double volume) {
    this.volume = volume;
  }

  public Double getPercentChange() {
    return percentChange;
  }

  public void setPercentChange(Double lastMove) {
    this.percentChange = lastMove;
  }


  public Double getAdjustedClose() {
    return adjustedClose;
  }

  public void setAdjustedClose(Double adjustedClose) {
    this.adjustedClose = adjustedClose;
  }

  public Double getPreviousAdjustedClose() {
    return previousAdjustedClose;
  }

  public void setPreviousAdjustedClose(Double previousAdjustedClose) {
    this.previousAdjustedClose = previousAdjustedClose;
  }

  public int getNbrDays() {
    return nbrDays;
  }

  public void setNbrDays(int nbrDays) {
    this.nbrDays = nbrDays;
  }
}
