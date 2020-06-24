package ch.nblotti.asset.index.to;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Table(name = "sp_sector_repartition")
@Entity
public class SPSectorResultTO implements Serializable {


  @Id
  @Column(name = "id")
  private Integer id;

  @Column(name = "date")
  private LocalDate date;

  @Column(name = "real_estate")
  private float realEstate;

  @Column(name = "percent_real_estate")
  private float percentRealEstate;

  @Column(name = "healthcare")
  private float healthcare;

  @Column(name = "percent_healthcare")
  private float percentHealthcare;


  @Column(name = "basic_materials")
  private float basicMaterials;

  @Column(name = "percent_basic_materials")
  private float percentBasicMaterials;


  @Column(name = "industrials")
  private float basicIndustrials;

  @Column(name = "percent_industrials")
  private float percentIndustrials;


  @Column(name = "consumer_cyclical")
  private float consumerCyclical;

  @Column(name = "percent_consumer_cyclical")
  private float percentConsumerCyclical;


  @Column(name = "energy")
  private float energy;

  @Column(name = "percent_energy")
  private float percentEnergy;


  @Column(name = "utilities")
  private float utilities;

  @Column(name = "percent_utilities")
  private float percentUtilities;


  @Column(name = "technology")
  private float technology;

  @Column(name = "percent_technology")
  private float percentTechnology;


  @Column(name = "consumer_defensive")
  private float consumerDefensive;

  @Column(name = "percent_consumer_defensive")
  private float percentConsumerDefensive;


  @Column(name = "financial_services")
  private float financialServices;

  @Column(name = "percent_financial_services")
  private float percentFinancialServices;


  @Column(name = "communication_services")
  private float communicationServices;

  @Column(name = "percent_communication_services")
  private float percentCommunicationServices;

  @Column(name = "total")
  private float total;


  public Integer getId() {
    return id;
  }

  public LocalDate getDate() {
    return date;
  }

  public float getRealEstate() {
    return realEstate;
  }

  public float getPercentRealEstate() {
    return percentRealEstate;
  }

  public float getHealthcare() {
    return healthcare;
  }

  public float getPercentHealthcare() {
    return percentHealthcare;
  }

  public float getBasicMaterials() {
    return basicMaterials;
  }

  public float getPercentBasicMaterials() {
    return percentBasicMaterials;
  }

  public float getBasicIndustrials() {
    return basicIndustrials;
  }

  public float getPercentIndustrials() {
    return percentIndustrials;
  }

  public float getConsumerCyclical() {
    return consumerCyclical;
  }

  public float getPercentConsumerCyclical() {
    return percentConsumerCyclical;
  }

  public float getEnergy() {
    return energy;
  }

  public float getPercentEnergy() {
    return percentEnergy;
  }

  public float getUtilities() {
    return utilities;
  }

  public float getPercentUtilities() {
    return percentUtilities;
  }

  public float getTechnology() {
    return technology;
  }

  public float getPercentTechnology() {
    return percentTechnology;
  }

  public float getConsumerDefensive() {
    return consumerDefensive;
  }

  public float getPercentConsumerDefensive() {
    return percentConsumerDefensive;
  }

  public float getFinancialServices() {
    return financialServices;
  }

  public float getPercentFinancialServices() {
    return percentFinancialServices;
  }

  public float getCommunicationServices() {
    return communicationServices;
  }

  public float getPercentCommunicationServices() {
    return percentCommunicationServices;
  }

  public float getTotal() {
    return total;
  }
}
