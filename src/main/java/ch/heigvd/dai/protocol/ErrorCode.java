package ch.heigvd.dai.protocol;

/** Codes d'erreur du protocole MC-serv (voir docs/protocol.md). */
public enum ErrorCode {
  NAME_TAKEN(1),
  NOT_FOUND(2),
  INVALID_BMP(3),
  BANK_EMPTY(4),
  UNKNOWN_COMMAND(5);

  private final int code;

  ErrorCode(int code) {
    this.code = code;
  }

  public int code() {
    return code;
  }
}
