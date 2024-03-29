/*
 * Copyright 2022 CozyDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package textmogrify
package benchmarks

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.io.file.{Files, Path}

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import textmogrify.lucene.AnalyzerPipe
import textmogrify.lucene.AnalyzerBuilder

/** To run the benchmark from within sbt:
  *
  * jmh:run -i 10 -wi 10 -f 2 -t 1 textmogrify.benchmarks.LuceneStreamingBenchmark
  *
  * Which means "10 iterations", "10 warm-up iterations", "2 forks", "1 thread". Please note that
  * benchmarks should be usually executed at least in 10 iterations (as a rule of thumb), but
  * more is better.
  */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class LuceneStreamingBenchmark {

  var asciiBytes: Array[Byte] = _
  @Setup
  def setup(): Unit =
    asciiBytes = Files[IO]
      .readAll(Path("../LICENSE"))
      .compile
      .to(Array)
      .unsafeRunSync()

  @Benchmark
  def tokenizeBytesTokenN1(): String = {
    val analyzer = AnalyzerBuilder.default.withLowerCasing.build[IO]
    val pipe = AnalyzerPipe.fromResource(analyzer)
    val bytes: Stream[IO, Byte] = Stream.emits(asciiBytes)
    pipe
      .tokenizeBytes(bytes, 1)
      .compile
      .last
      .unsafeRunSync()
      .get
  }

  @Benchmark
  def tokenizeBytesTokenN128(): String = {
    val analyzer = AnalyzerBuilder.default.withLowerCasing.build[IO]
    val pipe = AnalyzerPipe.fromResource(analyzer)
    val bytes: Stream[IO, Byte] = Stream.emits(asciiBytes)
    pipe
      .tokenizeBytes(bytes, 128)
      .compile
      .last
      .unsafeRunSync()
      .get
  }
}
