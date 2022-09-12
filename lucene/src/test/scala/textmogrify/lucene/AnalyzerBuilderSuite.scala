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

class FrenchAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "J'aime Les Jalapeños"
  val jumping = "Neeko aime sauter sur les compteurs"

  test("french analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.french
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("J'aime", "Les", "Jalapeños"))
  }

  test("french analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.french.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("j'aime", "les", "jalapeños"))
  }

  test("french analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.french.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("J'aime", "Les", "Jalapenos"))
  }

  test("french analyzer withStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.french.withStopWords(Set("Les"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("J'aime", "Jalapeños"))
  }

  test("french analyzer withPorterStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.french.withFrenchLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    // TODO: We should be able to prevent "Neeko" from being stemmed here with keyword support
    assertIO(actual, Vector("neko", "aime", "saut", "sur", "les", "compt"))
  }

  test("french analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.french.withFrenchLightStemmer
      .withStopWords(Set("on"))
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neko", "aime", "saut", "sur", "les", "compt"))
  }

}

class SpanishAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Me gustan los jalapeños"
  val jumping = "A Neeko le gusta saltar sobre los mostradores"

  test("spanish analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.spanish
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Me", "gustan", "los", "jalapeños"))
  }

  test("spanish analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.spanish.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("me", "gustan", "los", "jalapeños"))
  }

  test("spanish analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.spanish.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Me", "gustan", "los", "jalapenos"))
  }

  test("spanish analyzer withStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.spanish.withStopWords(Set("le", "los"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Me", "gustan", "jalapeños"))
  }

  test("spanish analyzer withSpanishLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.spanish.withSpanishLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    // TODO: We should be able to prevent "Neeko" from being stemmed here with keyword support
    assertIO(actual, Vector("a", "neek", "le", "gust", "saltar", "sobr", "los", "mostrador"))
  }

  test("spanish analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.spanish.withSpanishLightStemmer
      .withStopWords(Set("le", "los"))
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("a", "neek", "gust", "saltar", "sobr", "mostrador"))
  }

}
