## textmogrify

Textmogrify is a pre-alpha text manipulation library that hopefully works well with [fs2][fs2].

## Usage

This library is currently available for Scala binary versions 2.13 and 3.1.

To use the latest version, include the following in your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "io.pig" %% "textmogrify" % "@VERSION@"
)
```

## Lucene

Currently the core functionality of textmogrify is implemented with [Lucene][lucene] via the Lucene module.

```scala
libraryDependencies ++= Seq(
  "io.pig" %% "textmogrify-lucene" % "@VERSION@"
)
```

The Lucene module lets you use a Lucene [Analyzer][analyzer] to modify text, additionally it provides helpers to use `Analyzer`s with an fs2 [Stream][stream].

### Basics

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

We can see that our text was lowercased and the unicode `ñ` replaced with an ASCII `n`.


### Pipelines

Another common use is to construct a `Pipe`, or `Stream` to `Stream` function.
Let's say we have some messages we want to analyze and index as part of some search component.
Given a raw `Msg` type and an analyzed `Doc` type, we want to transform a `Stream[F, Msg]` into a `Stream[F, Doc]`.

```scala mdoc:silent
import fs2.Stream

case class Msg(id: Int, msg: String)
case class Doc(id: Int, tokens: Vector[String])

val input = Stream(
  Msg(0, "How do i trim my cats nails?"),
  Msg(1, "trimming cat nail"),
  Msg(2, "cat scratching furniture"),
)
```

```scala mdoc:silent
import fs2.Pipe

val normalizeMsgs: Pipe[IO, Msg, Doc] = msgs => {
  val tokenizer = AnalyzerBuilder.default
    .withLowerCasing
    .withStopWords(Set("how", "do", "i", "my"))
    .withPorterStemmer
    .tokenizer[IO]
  Stream.resource(tokenizer)
    .flatMap(f => msgs.evalMap(m => f(m.msg).map(ts => Doc(m.id, ts))))
}
```

We can then run our stream of `Msg` through our tokenizer `Pipe` to get our `Doc`s:

```scala mdoc:silent
val docs: Stream[IO, Doc] = input.through(normalizeMsgs)
```

```scala mdoc
docs.compile.toList.unsafeRunSync()
```

Be careful not to construct the `tokenizer` within a loop, we want to create it once and reuse it throughout the `Pipe`.


[analyzer]: https://lucene.apache.org/core/9_3_0/core/org/apache/lucene/analysis/Analyzer.html
[fs2]: https://fs2.io
[lucene]: https://lucene.apache.org/
[stream]: https://www.javadoc.io/doc/co.fs2/fs2-docs_2.13/latest/fs2/Stream.html
