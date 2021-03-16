package ch.nblotti.brasidas.index.composition;

class EODIndexCompositionDTO {


  String Code;

  String Name;

  String StartDate;

  String EndDate;

  String IsActiveNow;

  String IsDelisted;

  public EODIndexCompositionDTO() {
  }

  public String getCode() {
    return Code;
  }

  public void setCode(String code) {
    Code = code;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getStartDate() {
    return StartDate;
  }

  public void setStartDate(String startDate) {
    StartDate = startDate;
  }

  public String getEndDate() {
    return EndDate;
  }

  public void setEndDate(String endDate) {
    EndDate = endDate;
  }

  public String getIsActiveNow() {
    return IsActiveNow;
  }

  public void setIsActiveNow(String isActiveNow) {
    IsActiveNow = isActiveNow;
  }

  public String getIsDelisted() {
    return IsDelisted;
  }

  public void setIsDelisted(String isDelisted) {
    IsDelisted = isDelisted;
  }
}


