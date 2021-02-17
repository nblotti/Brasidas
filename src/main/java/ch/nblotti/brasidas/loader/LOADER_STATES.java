package ch.nblotti.brasidas.loader;

public enum LOADER_STATES {
  READY,
  GET_DATES,
  LOAD_NYSE,
  LOAD_NASDAQ,
  SAVE_FIRM,
  LOAD_INDEX,
  REFRESH_MAT_VIEWS,
  ERROR,
  DONE;

}
