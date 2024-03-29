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
package lucene

import munit.CatsEffectSuite
import cats.effect._
import org.apache.lucene.analysis.en.EnglishAnalyzer

class AnalyzerResourceSuite extends CatsEffectSuite {

  test("tokenizer should work") {
    val analyzer = AnalyzerResource.fromAnalyzer[IO](new EnglishAnalyzer())
    val tokenizer = Tokenizer.vectorTokenizer(analyzer)
    val actual = tokenizer.use { f =>
      f("Hello my name is Neeko")
    }
    assertIO(actual, Vector("hello", "my", "name", "neeko"))
  }

  test("tokenizer should yield a func that can be used multiple times") {
    val analyzer = AnalyzerResource.fromAnalyzer[IO](new EnglishAnalyzer())
    val tokenizer = Tokenizer.vectorTokenizer(analyzer)
    val actual = tokenizer.use { f =>
      for {
        v1 <- f("Hello my name is Neeko")
        v2 <- f("I enjoy jumping on counters")
      } yield v1 ++ v2

    }
    assertIO(actual, Vector("hello", "my", "name", "neeko", "i", "enjoi", "jump", "counter"))
  }

  test("tokenizer should work with custom Analyzer") {
    import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
    import org.apache.lucene.analysis.standard.StandardTokenizer
    import org.apache.lucene.analysis.en.PorterStemFilter
    import org.apache.lucene.analysis.LowerCaseFilter
    import org.apache.lucene.analysis.Analyzer

    val analyzer = AnalyzerResource.fromAnalyzer[IO](new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        val tokens = new LowerCaseFilter(source)
        new TokenStreamComponents(source, new PorterStemFilter(tokens))
      }
    })
    val tokenizer = Tokenizer.vectorTokenizer(analyzer)
    val actual = tokenizer.use { f =>
      for {
        v1 <- f("Hello my name is Neeko")
        v2 <- f("I enjoy jumping on counters")
      } yield (v1 ++ v2).mkString(" ")
    }
    assertIO(actual, "hello my name is neeko i enjoi jump on counter")
  }

}
