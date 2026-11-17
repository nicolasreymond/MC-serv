package ch.heigvd.dai.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RequestTest {

  @Test
  void parsesCommandWithoutArgs() {
    Request request = Request.parse("LIST");
    assertEquals(Request.Command.LIST, request.command());
    assertEquals(0, request.argCount());
  }

  @Test
  void parsesCommandWithArgs() {
    Request request = Request.parse("UPLOAD forest 352000");
    assertEquals(Request.Command.UPLOAD, request.command());
    assertArrayEquals(new String[] {"forest", "352000"}, request.args());
  }

  @Test
  void isCaseInsensitiveOnTheCommandName() {
    Request request = Request.parse("list");
    assertEquals(Request.Command.LIST, request.command());
  }

  @Test
  void collapsesRepeatedWhitespace() {
    Request request = Request.parse("UPLOAD   forest    352000");
    assertArrayEquals(new String[] {"forest", "352000"}, request.args());
  }

  @Test
  void unknownCommandNameGivesUnknown() {
    Request request = Request.parse("FOO bar");
    assertEquals(Request.Command.UNKNOWN, request.command());
  }

  @Test
  void blankOrNullLineGivesUnknown() {
    assertEquals(Request.Command.UNKNOWN, Request.parse("").command());
    assertEquals(Request.Command.UNKNOWN, Request.parse("   ").command());
    assertEquals(Request.Command.UNKNOWN, Request.parse(null).command());
  }
}
