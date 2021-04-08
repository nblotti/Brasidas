package ch.nblotti.brasidas.exchange.dayoffloader;

public enum DAYOFF_STATES {
  READY,
  WAITING_EVENT,
  LOAD_DAYOFF,
  SAVE_WEEKS,
  SAVE_MONTHS,
  ERROR_STATE,
  CANCELED,
  DONE;

}
