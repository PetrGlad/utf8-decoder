# Reactive non blocking parser for UTF-8 encoding

Usage example 
```java
class Utf8Printer {
  static void printParsed(byte[] utf8bytes) {
    final Utf8Parser parser = new Utf8Parser(System.out.print);
    parser.put(utf8bytes); // You can call this multiple times.
    parser.end(); // Check encoding at end of input stream.
  }
}
```

This parser allows incoming data be parsed incrementally without blocking current thread 
while waiting for incoming data.
With this parser you do not call a function to obtain input but instead call this parser
whenever new piece of input data is available.
The parser does perform input validation but not all disallowed overlong encodings 
might be detected.

The decoder is 2-3 times slower compared to OpenJDK UTF-8 decoder especially so on ASCII inputs
but hopefully allows to improve response time in exchange of lower throughput.

An advantage of this parser is handling partial code points without restarts.

Consider case when bytes of a code point are split between 2 adjacent byte buffers.
It is upsetting how often this case is overlooked especially in examples code.
Proper implementation cannot UTF-8 decode incoming buffers independently but should 
also consider this case. 

#### References

* https://tools.ietf.org/html/rfc3629
* https://en.wikipedia.org/wiki/UTF-8
