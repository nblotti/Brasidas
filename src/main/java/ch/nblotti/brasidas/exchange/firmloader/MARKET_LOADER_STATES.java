package ch.nblotti.brasidas.exchange.firmloader;

public enum MARKET_LOADER_STATES {
  READY,
  WAITING_EVENT,
  GET_DATES,
  LOAD_FORK,
  LOAD,
  LOAD_NYSE,
  LOAD_NYSE_END,
  LOAD_NASDAQ,
  LOAD_NASDAQ_END,
  LOAD_JOIN,
  CHOICE,
  CLEANUP,
  ERROR_STATE,
  ERROR,
  DONE;

}
