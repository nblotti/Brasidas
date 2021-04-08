package ch.nblotti.brasidas.exchange.splitloader;

public enum MARKET_SPLIT_STATES {
  READY,
  WAITING_EVENT,
  LOAD_SPLITS,
  UPDATE_QUOTES,
  ERROR_STATE,
  CANCELED,
  DONE;

}
