package net.readmarks.utf8;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Utf8Decoder2Test {
  /**
   * Compare the decoder's input with a reference implementation
   * (a {@link java.nio.charset.CharsetDecoder} from standard lib).
   *
   * @param isStrict Raise error if sample contains incorrect surrogate character pairs.
   * @param capacity decoding buffer capacity
   */
  static void checkSample(String sample, boolean isStrict, int chunk, int capacity) {
    assert chunk > 0;
    final byte[] bytes = sample.getBytes(StandardCharsets.UTF_8);
    final boolean standardEquals = sample.equals(new String(bytes, StandardCharsets.UTF_8));
    assertTrue(!isStrict || standardEquals); // Self test
    if (isStrict || standardEquals) {
      final StringBuilder parsed = new StringBuilder();
      final Utf8Decoder2 parser = new Utf8Decoder2(parsed::append, capacity);
      int pos = 0;
      while (pos < bytes.length) {
        int len = Math.min(chunk, bytes.length - pos);
        parser.put(bytes, pos, len);
        pos += chunk;
      }
      parser.end();
      assertEquals(sample, parsed.toString());
    }
  }

  @Test
  public void testParser1() {
    for (int c1 = Character.MIN_VALUE; c1 <= Character.MAX_VALUE; c1++) {
      checkSample(new String(new char[]{(char) c1}), false, 100, 10);
    }
  }

  @Test
  public void testSimpleCases() {
    checkSample("", true, 100, 100);
    checkSample("a", true, 100, 100);
    checkSample("Привет!", true, 100, 100);
  }

  @Test
  public void testParser3() {
    for (char c1 = 0; c1 < 500; c1++) {
      for (char c2 = 0; c2 < 300; c2++) {
        for (char c3 = 0; c3 < 50; c3++) {
          checkSample(new String(new char[]{c1, c2, c3}), false, 10, 6);
        }
      }
    }
  }

  @Test
  public void testBufferMultichunk() {
    for (int chunk = 1; chunk < 100; chunk++) {
      for (int bufferCapacity = 6; bufferCapacity < 100; bufferCapacity++) {
        checkSample("Привет!", true, chunk, bufferCapacity);
        checkSample("सायबरपंक जो हम लायक हैं", true, chunk, bufferCapacity);
        checkSample("सायबर私たちに値するサイバーパンクपंक जो हम लायक हैं", true, chunk, bufferCapacity);
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMalformedInput() {
    final Utf8Decoder2 p = new Utf8Decoder2(character -> {
    }, 88);
    p.put(new byte[]{
            (byte) 0xff
    });
    p.end();
    fail("Expecting exception.");
  }

  @Test(expected = IllegalStateException.class)
  public void stopAcceptingOnFail() {
    final Utf8Decoder2 p = new Utf8Decoder2(character -> {
    }, 33);
    try {
      p.put(new byte[]{
              (byte) 0xff
      });
      fail("Expecting exception.");
    } catch (IllegalArgumentException _e) {
      p.put(new byte[]{
              (byte) 'a'
      }); // Should not accept new data because failed before.
    }
  }
}
