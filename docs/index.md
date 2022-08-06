## textmogrify

Textmogrify is a pre-alpha text manipulation library that hopefully works well with [fs2][fs2].

### Usage

This library is currently available for Scala binary versions 2.13 and 3.1.

To use the latest version, include the following in your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "io.pig" %% "textmogrify" % "@VERSION@"
)
```

### Lucene

Currently the core functionality of textmogrify is implemented with [Lucene][lucene] via the Lucene module.

```scala
libraryDependencies ++= Seq(
  "io.pig" %% "textmogrify-lucene" % "@VERSION@"
)
```

The Lucene module lets you use a Lucene [`Analyzer`][analyzer] to modify text, additionally it provides helpers to use `Analyzer`s with an fs2 [`Stream`][stream].


[analyzer]: https://lucene.apache.org/core/9_3_0/core/org/apache/lucene/analysis/Analyzer.html
[fs2]: https://fs2.io
[lucene]: https://lucene.apache.org/
[stream]: https://www.javadoc.io/doc/co.fs2/fs2-docs_2.13/latest/fs2/Stream.html