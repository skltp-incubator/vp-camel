package se.skl.tp.vp.logging.logentry;

public enum LogLevelType {
  FATAL,
  ERROR,
  WARNING,
  INFO,
  DEBUG,
  TRACE;

  public String value() {
    return this.name();
  }

  public static LogLevelType fromValue(String v) {
    return valueOf(v);
  }
}
