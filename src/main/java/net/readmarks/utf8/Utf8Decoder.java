package net.readmarks.utf8;

/**
 * Reactively parses incoming byte sequences.
 * Call {@link #put(byte[])} method for each successive byte in input stream, when whole codepoint
 * is parsed consumer gets this value. If exception is thrown by the parser then all subsequent
 * inputs will cause exceptions (parsing will not continue after error).
 * <p>
 * Unicode UTF-8
 * <pre>
 *   0x00000000 .. 0x0000007F: 0xxxxxxx
 *   0x00000080 .. 0x000007FF: 110xxxxx 10xxxxxx
 *   0x00000800 .. 0x0000FFFF: 1110xxxx 10xxxxxx 10xxxxxx
 *   0x00010000 .. 0x001FFFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 *   0x00200000 .. 0x03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
 *   0x04000000 .. 0x7FFFFFFF: 111111xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
 * </pre>
 */
public class Utf8Decoder {

  public interface Consumer {
    void put(char ch);
  }
  private final Consumer out;

  private long inputPosition = 0;
  private int codePoint = -1;
  private static final int COUNT_INIT = -1;
  private static final int COUNT_ERR = -2;
  private static final int COUNT_MAX = 6;
  private int remainingCount = COUNT_INIT;
  private int requiredCount;

  int bitPrefixCount = -1; // is a class member for testing
  int maskedVal = -1; // is a class member for testing

  public Utf8Decoder(Consumer out) {
    this.out = out;
  }

  void matchMask(final byte x) {
    int mask = 0x80;
    int count = 0;
    while (count <= COUNT_MAX && ((x & mask) != 0)) {
      mask >>= 1;
      count++;
    }
    if (count > COUNT_MAX) {
      remainingCount = COUNT_ERR;
      throw new IllegalArgumentException("UTF8 encoding error: Unexpected prefix"
                                           + " in octet 0x" + Integer.toHexString(x) + " at " + inputPosition);
    }
    bitPrefixCount = count;
    maskedVal = x & (mask - 1);
  }

  /**
   * Call this with every successive piece of input stream octets.
   * @param xs next byte in input stream.
   * @see #end()
   */
  public void put(final byte[] xs) {
    if (remainingCount == COUNT_ERR) {
      throw new IllegalStateException("UTF8 decoder is in inconsistent state"
                                        + " (see previous errors at " + inputPosition + ").");
    }
    for (byte x : xs) {
      matchMask(x);
      if (remainingCount == COUNT_INIT) {
        if (bitPrefixCount == 0) {
          out.put((char) x); // ASCII subset
        } else if (bitPrefixCount == 1) {
          remainingCount = COUNT_ERR;
          throw new IllegalArgumentException("UTF8 encoding error: continuation octet in starting position."
                                               + " octet 0x" + Integer.toHexString(x) + " at " + inputPosition);
        } else {
          codePoint = maskedVal;
          remainingCount = bitPrefixCount - 1;
          requiredCount = remainingCount;
        }
      } else if (remainingCount > 0) {
        assert bitPrefixCount == 1;
        codePoint = (codePoint << 6) | maskedVal;
        remainingCount--;
        if (remainingCount == 0) {
          if (requiredCount == 2 && (codePoint == 0xC0 || codePoint == 0xC1)) {
            throw new IllegalArgumentException("UTF8 encoding error: overlong encoding for code point "
                                                 + Integer.toHexString(codePoint) + " at " + inputPosition);
          }
          if (Character.isBmpCodePoint(codePoint)) {
            out.put((char) codePoint);
          } else if (Character.isValidCodePoint(codePoint)) {
            out.put(Character.highSurrogate(codePoint));
            out.put(Character.lowSurrogate(codePoint));
          } else {
            throw new IllegalArgumentException("UTF8 encoding error: invalid code point "
                                                 + Integer.toHexString(codePoint) + " at " + inputPosition);
          }
          remainingCount = COUNT_INIT;
        }
      } else {
        final int c = remainingCount;
        remainingCount = COUNT_ERR;
        throw new IllegalStateException("UTF8 decoder error: Remaining count " + c);
      }
      inputPosition++;
    }
  }

  /**
   * Call this when there's no more input data
   */
  public void end() {
    if (remainingCount > 0) {
      throw new IllegalArgumentException("UTF8 encoding error: premature end of input stream."
                                           + " Expecting " + remainingCount + " more octets.");
    }
  }
}
