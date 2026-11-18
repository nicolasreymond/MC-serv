package ch.heigvd.dai.protocol;

/** Formatte les reponses du protocole MC-serv (voir docs/protocol.md). */
public final class Response {

  private Response() {
  }

  /** "OK\n" -- succes sans donnees supplementaires (UPLOAD, REMOVE). */
  public static String ok() {
    return "OK\n";
  }

  /** "OK <rest>\n" -- succes avec des donnees (LIST, DOWNLOAD, MOSAIC). */
  public static String ok(String rest) {
    return "OK " + rest + "\n";
  }

  /** "ERROR <code>\n" */
  public static String error(ErrorCode code) {
    return "ERROR " + code.code() + "\n";
  }
}
