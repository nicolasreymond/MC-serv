package ch.heigvd.dai.protocol;

import java.util.Arrays;

/**
 * Une ligne de commande du protocole, deja decoupee en commande + arguments.
 * Ne touche a aucun flux reseau : construite a partir d'une simple chaine,
 * ce qui la rend testable sans socket (voir docs/protocol.md section 3).
 */
public record Request(Command command, String[] args) {

  public enum Command {
    LIST,
    UPLOAD,
    DOWNLOAD,
    REMOVE,
    MOSAIC,
    UNKNOWN
  }

  /** Parse une ligne recue (sans le '\n' final). Jamais d'exception : une
   * commande invalide ou inconnue donne Command.UNKNOWN. */
  public static Request parse(String line) {
    if (line == null || line.isBlank()) {
      return new Request(Command.UNKNOWN, new String[0]);
    }

    String[] parts = line.trim().split("\\s+");
    Command cmd;
    try {
      cmd = Command.valueOf(parts[0].toUpperCase());
    } catch (IllegalArgumentException e) {
      return new Request(Command.UNKNOWN, new String[0]);
    }

    return new Request(cmd, Arrays.copyOfRange(parts, 1, parts.length));
  }

  public int argCount() {
    return args.length;
  }

  public String arg(int index) {
    return args[index];
  }
}
