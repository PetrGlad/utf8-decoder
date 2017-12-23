# Reactive non blocking parser for UTF-8 encoding

 [ ![Download](https://api.bintray.com/packages/petrglad/readmarks/utf8-decoder/images/download.svg) ](https://bintray.com/petrglad/readmarks/utf8-decoder/_latestVersion)

Dependency for build.gradle:
```groovy
repositories {
  jcenter()
}

dependencies {
      compile 'net.readmarks:utf8-decoder:2.0.0'
}
```

Usage example 
```java
import net.readmarks.utf8.Utf8Decoder2;
class Utf8Printer {
  static void printDecodedUtf8(byte[] utf8bytes) {
    final Utf8decoder parser = new Utf8decoder(System.out.print, 2048);
    parser.put(utf8bytes); // You can call this multiple times.
    parser.end(); // Check encoding at end of input stream.
  }
}
```

This parser allows incoming data be parsed incrementally without blocking current thread 
while waiting for incoming data.
With this parser you do not call a function to obtain input but instead call this parser
whenever new piece of input data is available.

#### References

* https://tools.ietf.org/html/rfc3629
* https://en.wikipedia.org/wiki/UTF-8
