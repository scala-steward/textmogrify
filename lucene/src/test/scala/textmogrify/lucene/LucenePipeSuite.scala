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
import org.apache.lucene.analysis.en.EnglishAnalyzer

class AnalyzerPipeSuite extends CatsEffectSuite {

  def input = Stream("Hello my name is Neeko,", "I enjoy jumping on counters.")

  test("fromAnalyzer should accept basic Analyzer") {
    val analyzer = AnalyzerPipe.fromAnalyzer[IO](new EnglishAnalyzer())
    val actual = analyzer.tokenizeStrings(input, 1).take(1).compile.toList
    assertIO(actual, List("hello"))
  }

  test("tokenizeStrings should chunk on tokenN") {
    val analyzer = AnalyzerPipe.fromAnalyzer[IO](new EnglishAnalyzer())
    val actual = analyzer.tokenizeStrings(input, 3).take(6).chunks.compile.toVector
    assertIO(actual, Vector(Chunk("hello", "my", "name"), Chunk("neeko", "i", "enjoi")))
  }

  test("tokenizeStrings works with custom Analyzers") {
    import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
    import org.apache.lucene.analysis.standard.StandardTokenizer
    import org.apache.lucene.analysis.en.PorterStemFilter
    import org.apache.lucene.analysis.LowerCaseFilter
    import org.apache.lucene.analysis.Analyzer

    val stemmer = AnalyzerPipe.fromAnalyzer[IO](new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        val tokens = new LowerCaseFilter(source)
        new TokenStreamComponents(source, new PorterStemFilter(tokens))
      }
    })
    val actual = stemmer.tokenizeStrings(input, 3).take(10).compile.toVector
    assertIO(
      actual,
      Vector("hello", "my", "name", "is", "neeko", "i", "enjoi", "jump", "on", "counter"),
    )
  }

  test("tokenizeStrings works on infinite input streams") {
    val analyzer = AnalyzerPipe.fromAnalyzer[IO](new EnglishAnalyzer())
    val infInput = input.repeat
    val actual = analyzer.tokenizeStrings(infInput, 50).take(10).compile.toVector
    assertIO(
      actual,
      Vector("hello", "my", "name", "neeko", "i", "enjoi", "jump", "counter", "hello", "my"),
    )
  }

  test("tokenizeStringsRaw does not intersperse spaces between elements") {
    val input = Stream("combined", "word")
    val analyzer = AnalyzerPipe.fromAnalyzer[IO](new EnglishAnalyzer())
    val actual = analyzer.tokenizeStringsRaw(input, 10).take(1).compile.toVector
    assertIO(actual, Vector("combinedword"))
  }

}
