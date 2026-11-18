package ch.heigvd.dai.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResponseTest {

  @Test
  void okWithoutDataEndsWithNewline() {
    assertEquals("OK\n", Response.ok());
  }

  @Test
  void okWithDataPutsASpaceBeforeIt() {
    assertEquals("OK 2\nforest 400x300\n", Response.ok("2\nforest 400x300"));
  }

  @Test
  void errorUsesTheCodeFromErrorCode() {
    assertEquals("ERROR 1\n", Response.error(ErrorCode.NAME_TAKEN));
    assertEquals("ERROR 5\n", Response.error(ErrorCode.UNKNOWN_COMMAND));
  }
}
