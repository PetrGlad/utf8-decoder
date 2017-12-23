package net.readmarks.utf8;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class Microbenchmark {
  public static void main(String[] argv) {
    final Random rand = new Random(0xDEADBEEF);
    final int n = 1024 * 1024 * 10;
    final StringBuilder sb = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      sb.append((char)rand.nextInt(Character.MAX_CODE_POINT));
    }
    System.out.println("Sample size " + n + ", encoded size " + sb.length());

    final byte[] utf8Encoded = sb.toString().getBytes(StandardCharsets.UTF_8);
    for (int ll = 1; ll < 80; ll++) {
      final long t1 = System.currentTimeMillis();
      assertEquals(sb.length(), new String(utf8Encoded, StandardCharsets.UTF_8).length());
      System.out.println("(nio.charset) " + getElapsed(utf8Encoded.length, t1) + " mS/Mb");
    }
    System.out.println();
    for (int ll = 0; ll < 80; ll++) {
      final StringBuilder parsed = new StringBuilder(n * 10);

      final long t1 = System.currentTimeMillis();
      final Utf8Decoder parser = new Utf8Decoder(parsed::append);
      parser.put(utf8Encoded);
      parser.end();
      assertEquals(sb.length(), parsed.length());
      System.out.println("(v1) " + (getElapsed(utf8Encoded.length, t1)) + " mS/Mb");
    }
    System.out.println();
    for (int ll = 0; ll < 80; ll++) {
      final StringBuilder parsed = new StringBuilder(n * 10);

      final long t1 = System.currentTimeMillis();
      final Utf8Decoder2 parser = new Utf8Decoder2(parsed::append, 2048);
      parser.put(utf8Encoded, 0, utf8Encoded.length);
      parser.end();
      assertEquals(sb.length(), parsed.length());
      System.out.println("(v2) " + (getElapsed(utf8Encoded.length, t1)) + " mS/Mb");
    }
  }

  private static double getElapsed(int length, long t1) {
    return (System.currentTimeMillis() - t1) / (length / 1024.0 / 1024.0);
  }
}
