package ch.nblotti.brasidas.common;

public class LoadingEvent {


  private String start, end;
  private String process;
  private STATUS status;

  public LoadingEvent() {

  }

  public LoadingEvent(String process, STATUS status, String start, String end) {
    this();
    this.start = start;
    this.end = end;
    this.process = process;
    this.status = status;
  }

  public String getStart() {
    return start;
  }

  public String getEnd() {
    return end;
  }

  public String getProcess() {
    return process;
  }

  public STATUS getStatus() {
    return status;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public void setEnd(String end) {
    this.end = end;
  }

  public void setProcess(String process) {
    this.process = process;
  }

  public void setStatus(STATUS status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return String.format("LoadingEvent{process=%s,status=%s,start=%s,end=%s}", getProcess(), getStatus(), getStart(), getEnd());
  }

  static enum STATUS {
    SUCCESS,
    FAILURE
  }

}
