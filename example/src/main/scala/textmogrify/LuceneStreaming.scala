/*
 * Copyright 2022 Pig.io
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

import textmogrify.lucene.{AnalyzerBuilder, AnalyzerPipe}
import fs2.Stream
import fs2.io.file.{Files, Path}
import cats.effect.{IO, IOApp}

object LuceneStreaming extends IOApp.Simple {

  def input: Stream[IO, Byte] = Files[IO].readAll(Path("LICENSE"))

  val analyzer = AnalyzerBuilder.english.withLowerCasing.build[IO]
  val analyzerPipe = AnalyzerPipe.fromResource(analyzer)

  val run = analyzerPipe.tokenizeBytes(input, 16).take(8).compile.toList.flatMap(IO.println)

}
