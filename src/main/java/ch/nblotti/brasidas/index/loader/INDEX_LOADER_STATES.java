package ch.nblotti.brasidas.index.loader;

public enum INDEX_LOADER_STATES {
  READY,
  WAITING_EVENT,
  GET_DATES,
  LOAD_INDEX,
  CLEANUP,
  ERROR_STATE,
  ERROR,
  DONE;

}
