package ch.nblotti.brasidas.exchange.loader;

public enum CLEANUP_STATES {
  READY,
  WAITING_EVENT,
  GET_DATES,
  DELETE_STATE,
  ERROR_STATE,
  DONE,
  CANCELED;

}
