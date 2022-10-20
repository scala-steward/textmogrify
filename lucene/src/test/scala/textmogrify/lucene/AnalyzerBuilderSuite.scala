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

class DefaultAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "I Like Jalapeños"

  test("default analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.default
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("I", "Like", "Jalapeños"))
  }

  test("default analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.default.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("i", "like", "jalapeños"))
  }

  test("default analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.default.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("I", "Like", "Jalapenos"))
  }

  test("default analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.default.withCustomStopWords(Set("I"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Like", "Jalapeños"))
  }

}

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

  test("english analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.english.withCustomStopWords(Set("I"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Like", "Jalapeños"))
  }

  test("english analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.english.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "likes", "jumping", "counters"))
  }

  test("english analyzer withPorterStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.english.withPorterStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "like", "jump", "on", "counter"))
  }

  test("english analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.english.withPorterStemmer
      .withCustomStopWords(Set("counters"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "like", "jump"))
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

  test("french analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.french.withCustomStopWords(Set("Les"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("J'aime", "Jalapeños"))
  }

  test("french analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.french.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "aime", "sauter", "compteurs"))
  }

  test("french analyzer withFrenchLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.french.withFrenchLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    // TODO: We should be able to prevent "Neeko" from being stemmed here with keyword support
    assertIO(actual, Vector("neko", "aime", "saut", "sur", "les", "compt"))
  }

  test("french analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.french.withFrenchLightStemmer
      .withCustomStopWords(Set("neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("aime", "saut", "compt"))
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

  test("spanish analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.spanish.withCustomStopWords(Set("le", "los"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Me", "gustan", "jalapeños"))
  }

  test("spanish analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.spanish.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("A", "Neeko", "gusta", "saltar", "mostradores"))
  }

  test("spanish analyzer withSpanishLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.spanish.withSpanishLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    // TODO: We should be able to prevent "Neeko" from being stemmed here with keyword support
    assertIO(actual, Vector("a", "neek", "le", "gust", "saltar", "sobr", "los", "mostrador"))
  }

  test("spanish analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.spanish.withSpanishLightStemmer
      .withCustomStopWords(Set("neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("gust", "saltar", "mostrador"))
  }

}

class ItalianAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Mi piacciono i jalapeños"
  val jumping = "A Neeko piace saltare sui contatori"

  test("italian analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.italian
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Mi", "piacciono", "i", "jalapeños"))
  }

  test("italian analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.italian.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("mi", "piacciono", "i", "jalapeños"))
  }

  test("italian analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.italian.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Mi", "piacciono", "i", "jalapenos"))
  }

  test("italian analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.italian.withCustomStopWords(Set("i"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Mi", "piacciono", "jalapeños"))
  }

  test("italian analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.italian.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("A", "Neeko", "piace", "saltare", "contatori"))
  }

  test("italian analyzer withItalianLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.italian.withItalianLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("a", "neeko", "piace", "saltar", "sui", "contator"))
  }

  test("italian analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.italian.withItalianLightStemmer
      .withCustomStopWords(Set("neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("piace", "saltar", "contator"))
  }

}

class GermanAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Ich mag Jalapeños"
  val jumping = "Neeko springt gerne auf Theken"

  test("german analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.german
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Ich", "mag", "Jalapeños"))
  }

  test("german analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.german.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("ich", "mag", "jalapeños"))
  }

  test("german analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.german.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Ich", "mag", "Jalapenos"))
  }

  test("german analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.german.withCustomStopWords(Set("Ich"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("mag", "Jalapeños"))
  }

  test("german analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.german.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "springt", "gerne", "Theken"))
  }

  test("german analyzer withGermanLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.german.withGermanLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "springt", "gern", "auf", "thek"))
  }

  test("german analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.german.withGermanLightStemmer
      .withCustomStopWords(Set("Neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("springt", "gern", "thek"))
  }

}

class DutchAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Ik hou van Jalapeños"
  val jumping = "Neeko springt graag op balies"

  test("dutch analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.dutch
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Ik", "hou", "van", "Jalapeños"))
  }

  test("dutch analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.dutch.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("ik", "hou", "van", "jalapeños"))
  }

  test("dutch analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.dutch.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Ik", "hou", "van", "Jalapenos"))
  }

  test("dutch analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.dutch.withCustomStopWords(Set("Ik"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("hou", "van", "Jalapeños"))
  }

  test("dutch analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.dutch.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "springt", "graag", "balies"))
  }

  test("dutch analyzer withDutchLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.dutch.withDutchStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neeko", "springt", "grag", "op", "balies"))
  }

  test("dutch analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.dutch.withDutchStemmer
      .withCustomStopWords(Set("Neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("springt", "grag", "op", "balies"))
  }

}

class PortugueseAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Eu gosto de jalapeños"
  val jumping = "Neeko gosta de saltar em balcões"

  test("portuguese analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.portuguese
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Eu", "gosto", "de", "jalapeños"))
  }

  test("portuguese analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.portuguese.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("eu", "gosto", "de", "jalapeños"))
  }

  test("portuguese analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.portuguese.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Eu", "gosto", "de", "jalapenos"))
  }

  test("portuguese analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.portuguese.withCustomStopWords(Set("eu", "de"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("gosto", "jalapeños"))
  }

  test("portuguese analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.portuguese.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "gosta", "saltar", "balcões"))
  }

  test("portuguese analyzer withPortugueseLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.portuguese.withPortugueseLightStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neek", "gost", "de", "saltar", "em", "balca"))
  }

  test("portuguese analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.portuguese.withPortugueseLightStemmer
      .withCustomStopWords(Set("Neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("gost", "saltar", "balco"))
  }

}

class BrazilianPortugueseAnalyzerBuilderSuite extends CatsEffectSuite {

  val jalapenos = "Eu gosto de jalapeños"
  val jumping = "Neeko gosta de pular em balcões"

  test("brazilianPortuguese analyzer default should tokenize without any transformations") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Eu", "gosto", "de", "jalapeños"))
  }

  test("brazilianPortuguese analyzer withLowerCasing should lowercase all letters") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("eu", "gosto", "de", "jalapeños"))
  }

  test("brazilianPortuguese analyzer withASCIIFolding should fold 'ñ' to 'n'") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withASCIIFolding
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("Eu", "gosto", "de", "jalapenos"))
  }

  test("brazilianPortuguese analyzer withCustomStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withCustomStopWords(Set("eu", "de"))
    val actual = analyzer.tokenizer[IO].use(f => f(jalapenos))
    assertIO(actual, Vector("gosto", "jalapeños"))
  }

  test("brazilianPortuguese analyzer withDefaultStopWords should filter them out") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withDefaultStopWords
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("Neeko", "gosta", "pular", "balcões"))
  }

  test("brazilianPortuguese analyzer withPortugueseLightStemmer should lowercase and stem words") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withBrazilianStemmer
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("neek", "gost", "de", "pul", "em", "balco"))
  }

  test("brazilianPortuguese analyzer builder settings can be chained") {
    val analyzer = AnalyzerBuilder.brazilianPortuguese.withBrazilianStemmer
      .withCustomStopWords(Set("Neeko"))
      .withDefaultStopWords
      .withASCIIFolding
      .withLowerCasing
    val actual = analyzer.tokenizer[IO].use(f => f(jumping))
    assertIO(actual, Vector("gost", "pul", "balco"))
  }

}
