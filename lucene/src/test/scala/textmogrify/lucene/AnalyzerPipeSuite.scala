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
package lucene

import fs2.{Stream, Chunk}
import munit.CatsEffectSuite
import cats.effect._

class AnalyzerPipeSuite extends CatsEffectSuite {

  def input = Stream("Hello my name is Neeko,", "I enjoy jumping on counters.")

  test("fromAnalyzer should accept basic Analyzer") {
    val analyzer = AnalyzerPipe.fromResource[IO](AnalyzerBuilder.english.build)
    val actual = analyzer.tokenizeStrings(input, 1).take(1).compile.toList
    assertIO(actual, List("Hello"))
  }

  test("tokenizeStrings should chunk on tokenN") {
    val analyzer = AnalyzerPipe.fromResource[IO](AnalyzerBuilder.english.build)
    val actual = analyzer.tokenizeStrings(input, 3).take(6).chunks.compile.toVector
    assertIO(actual, Vector(Chunk("Hello", "my", "name"), Chunk("is", "Neeko", "I")))
  }

  test("tokenizeStrings works on infinite input streams") {
    val analyzer = AnalyzerPipe.fromResource[IO](AnalyzerBuilder.english.build)
    val infInput = input.repeat
    val actual = analyzer.tokenizeStrings(infInput, 50).take(12).compile.toVector
    assertIO(
      actual,
      Vector(
        "Hello",
        "my",
        "name",
        "is",
        "Neeko",
        "I",
        "enjoy",
        "jumping",
        "on",
        "counters",
        "Hello",
        "my",
      ),
    )
  }

  test("tokenizeStringsRaw does not intersperse spaces between elements") {
    val input = Stream("combined", "word")
    val analyzer = AnalyzerPipe.fromResource[IO](AnalyzerBuilder.english.build)
    val actual = analyzer.tokenizeStringsRaw(input, 10).take(1).compile.toVector
    assertIO(actual, Vector("combinedword"))
  }

}
