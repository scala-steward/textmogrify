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

import munit.CatsEffectSuite
import cats.effect._

class EnglishAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "I Like Jalapeños"
  val jumping = "Neeko likes jumping on counters"

  test("english analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.english
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("I", "Like", "Jalapeños"))
  }

  test("english analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.english.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("i", "like", "jalapeños"))
  }

  test("english analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.english.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("I", "Like", "Jalapenos"))
  }

  test("english analyzer withStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.english.withStopWords(Set("I"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Like", "Jalapeños"))
  }

  test("english analyzer withPorterStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.english.withPorterStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "like", "jump", "on", "counter"))
  }

  test("english analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.english.withPorterStemmer
      .withStopWords(Set("on"))
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "like", "jump", "counter"))
  }

}
