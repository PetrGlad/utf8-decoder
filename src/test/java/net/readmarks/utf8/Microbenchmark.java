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
      sb.append((char) rand.nextInt(Character.MAX_CODE_POINT));
    }
    final byte[] utf8Encoded = sb.toString().getBytes(StandardCharsets.UTF_8);
    for (int ll = 1; ll < 80; ll++) {
      final long t1 = System.currentTimeMillis();
      assertEquals(sb.length(), new String(utf8Encoded, StandardCharsets.UTF_8).length());
      System.out.println("(default) " + ((System.currentTimeMillis() - t1) / 10.0) + " mS/Mb");
    }
    System.out.println();
    for (int ll = 0; ll < 80; ll++) {
      final long t1 = System.currentTimeMillis();
      final StringBuilder parsed = new StringBuilder(n);
      final Utf8Parser parser = new Utf8Parser(parsed::append);
      parser.put(utf8Encoded);
      parser.end();
      assertEquals(sb.length(), parsed.length());
      System.out.println("(this) " + ((System.currentTimeMillis() - t1) / 10.0) + " mS/Mb");
    }
  }
}
