package net.readmarks.utf8;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * Reactively parses incoming byte sequences.
 * Call {@link #put(byte[], int, int)} method for each successive byte sequence in input stream.
 * When whole codepoint is parsed consumer is called with this value.
 * When decoding exception is thrown by this parser all subsequent
 * input will throw exceptions (parsing will not continue after error).
 */
public class Utf8Decoder2 {

  public interface Consumer {
    void put(CharSequence ch);
  }

  private final Consumer out;
  private final ByteBuffer byteBuf;
  private final CharBuffer charBuf;
  private final CharsetDecoder decoder;
  private boolean stopped = false;

  public Utf8Decoder2(Consumer out, int capacity) {
    this.out = out;
    assert capacity >= 6;
    byteBuf = ByteBuffer.allocate(capacity);
    assert byteBuf.hasArray();
    charBuf = CharBuffer.allocate(capacity);
    assert charBuf.hasArray();
    decoder = StandardCharsets.UTF_8.newDecoder();
  }

  public void put(final byte[] xs) {
    put(xs, 0, xs.length);
  }

  public void put(final byte[] xs, int offset, final int len) {
    if (stopped) {
      throw new IllegalStateException("Decoder is finished.");
    }
    try {
      int remainder = len;
      while (remainder > 0) {
        int decodedCount = decodeBytes(xs, offset, remainder);
        assert decodedCount > 0;
        offset += decodedCount;
        remainder -= decodedCount;
      }
    } catch (CharacterCodingException e) {
      stopped = true;
      throw new IllegalArgumentException(e);
    }
  }

  private int fillBuffer(final byte[] xs, int offset, final int len) {
    final int pos = byteBuf.position();
    final int count = Math.min(len, byteBuf.capacity() - pos);
    System.arraycopy(xs, offset, byteBuf.array(), pos, count);
    byteBuf.position(pos + count);
    return count;
  }

  private int decodeBytes(final byte[] xs, int offset, final int len) throws CharacterCodingException {
    int count = fillBuffer(xs, offset, len);
    decodeBuffer(false);
    return count;
  }

  private void decodeBuffer(boolean finalChunk) throws CharacterCodingException {
    byteBuf.flip();
    throwDecodeException(decoder.decode(byteBuf, charBuf, finalChunk));
    if (finalChunk) {
      throwDecodeException(decoder.flush(charBuf));
    }
    byteBuf.compact();
    charBuf.flip();
    out.put(charBuf.toString());
    charBuf.clear();
  }

  private void throwDecodeException(CoderResult result) throws CharacterCodingException {
    if (result.isMalformed())
      throw new MalformedInputException(result.length());
    if (result.isUnmappable())
      throw new UnmappableCharacterException(result.length());
    if (result.isOverflow()) {
      // This is not expected: Char buffer should be large enough
      throw new IllegalStateException("Output buffer overflow.");
    }
  }

  /**
   * Call this when there's no more input data.
   */
  public void end() {
    if (stopped) {
      throw new IllegalStateException("Decoder is finished.");
    }
    try {
      decodeBuffer(true);
    } catch (CharacterCodingException e) {
      throw new IllegalArgumentException(e);
    } finally {
      stopped = true;
    }
  }
}
