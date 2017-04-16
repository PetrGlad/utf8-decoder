# Reactive parser for UTF-8 encoding

Usage example 
```
class Utf8Printer {
  static void printParsed(byte[] utf8bytes) {
    final Utf8Parser parser = new Utf8Parser(System.out.print);
    parser.put(utf8bytes); // You can call this multiple times.
    parser.end(); // Check encoding at end of input stream.
  }
}
```
This parser allows incoming data be parsed incrementally without blocking current thread while waining for incoming data.
With this parser you do not call a function to obtain input but instead call this parser whenever new piece 
of input data is available.

The decoder is 2-3 times slower compared to OpenJDK UTF-8 decoder especially so on ASCII inputs
but hopefully allows to improve response time in exchange of lower throughput.

The parser does perform input validation but not all disallowed overlong encodings might be detected.

### References

* https://tools.ietf.org/html/rfc3629
* https://en.wikipedia.org/wiki/UTF-8
