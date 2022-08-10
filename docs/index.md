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


Typical usage is to use the `AnalyzerBuilder` to configure an `Analyzer` and call `.tokenizer` to get a `Resource[F, String => F[Vector[String]]]`:

```scala mdoc:silent
import textmogrify.lucene.AnalyzerBuilder
import cats.effect.IO

val tokenizer = AnalyzerBuilder.default.withLowerCasing.withASCIIFolding.tokenizer[IO]

val tokens: IO[Vector[String]] = tokenizer.use(
  f => f("I Like Jalapeños")
)
```

Because this documentation is running in mdoc, we'll import an IO runtime and run explicitly:

```scala mdoc
import cats.effect.unsafe.implicits.global

tokens.unsafeRunSync()
```


[analyzer]: https://lucene.apache.org/core/9_3_0/core/org/apache/lucene/analysis/Analyzer.html
[fs2]: https://fs2.io
[lucene]: https://lucene.apache.org/
[stream]: https://www.javadoc.io/doc/co.fs2/fs2-docs_2.13/latest/fs2/Stream.html