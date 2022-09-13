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

package textmogrify.lucene

import cats.effect.kernel.{Resource, Sync}
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.es.SpanishLightStemFilter
import org.apache.lucene.analysis.fr.FrenchLightStemFilter
import org.apache.lucene.analysis.it.ItalianLightStemFilter
import org.apache.lucene.analysis.de.GermanLightStemFilter
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.StopFilter
import org.apache.lucene.analysis.TokenStream

final case class Config(
    lowerCase: Boolean,
    foldASCII: Boolean,
    stopWords: Set[String],
) {
  def withLowerCasing: Config =
    copy(lowerCase = true)

  def withASCIIFolding: Config =
    copy(foldASCII = true)

  def withStopWords(words: Set[String]): Config =
    copy(stopWords = words)
}
object Config {
  def empty: Config = Config(false, false, Set.empty)
}

/** Build an Analyzer or tokenizer function */
sealed abstract class AnalyzerBuilder private[lucene] (config: Config) {
  type Builder <: AnalyzerBuilder

  def withConfig(config: Config): Builder

  /** Adds a lowercasing stage to the analyzer pipeline */
  def withLowerCasing: Builder =
    withConfig(config.withLowerCasing)

  /** Adds an ASCII folding stage to the analyzer pipeline
    * ASCII folding converts alphanumeric and symbolic Unicode characters into
    * their ASCII equivalents, if one exists.
    */
  def withASCIIFolding: Builder =
    withConfig(config.withASCIIFolding)

  /** Adds a stop filter stage to analyzer pipeline for non-empty sets. */
  def withStopWords(words: Set[String]): Builder =
    withConfig(config.withStopWords(words))

  /** Build the Analyzer wrapped inside a Resource. */
  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer]

  /** Directly construct a tokenizing function
    */
  def tokenizer[F[_]](implicit F: Sync[F]): Resource[F, String => F[Vector[String]]] =
    build.map(a => Tokenizer.vectorTokenizer(a))

  private[lucene] def mkFromStandardTokenizer[F[_]](
      config: Config
  )(extras: TokenStream => TokenStream)(implicit F: Sync[F]): Resource[F, Analyzer] =
    Resource.make(F.delay(new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        var tokens = if (config.lowerCase) new LowerCaseFilter(source) else source
        tokens = if (config.foldASCII) new ASCIIFoldingFilter(tokens) else tokens
        tokens =
          if (config.stopWords.isEmpty) tokens
          else {
            val stopSet = new CharArraySet(config.stopWords.size, true)
            config.stopWords.foreach(w => stopSet.add(w))
            new StopFilter(tokens, stopSet)
          }
        new TokenStreamComponents(source, extras(tokens))
      }
    }))(analyzer => F.delay(analyzer.close()))

}
object AnalyzerBuilder {
  def default: DefaultAnalyzerBuilder =
    new DefaultAnalyzerBuilder(Config.empty)
  def english: EnglishAnalyzerBuilder =
    new EnglishAnalyzerBuilder(Config.empty, false)
  def french: FrenchAnalyzerBuilder =
    new FrenchAnalyzerBuilder(Config.empty, false)
  def german: GermanAnalyzerBuilder =
    new GermanAnalyzerBuilder(Config.empty, false)
  def italian: ItalianAnalyzerBuilder =
    new ItalianAnalyzerBuilder(Config.empty, false)
  def spanish: SpanishAnalyzerBuilder =
    new SpanishAnalyzerBuilder(Config.empty, false)
}

final class DefaultAnalyzerBuilder private[lucene] (config: Config)
    extends AnalyzerBuilder(config) { self =>
  type Builder = DefaultAnalyzerBuilder

  def withConfig(newConfig: Config): DefaultAnalyzerBuilder =
    new DefaultAnalyzerBuilder(newConfig)

  def english: EnglishAnalyzerBuilder =
    new EnglishAnalyzerBuilder(config, false)

  def french: FrenchAnalyzerBuilder =
    new FrenchAnalyzerBuilder(config, false)

  def spanish: SpanishAnalyzerBuilder =
    new SpanishAnalyzerBuilder(config, false)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(identity)
}

final class EnglishAnalyzerBuilder private[lucene] (
    config: Config,
    stemmer: Boolean,
) extends AnalyzerBuilder(config) { self =>
  type Builder = EnglishAnalyzerBuilder

  private def copy(
      newConfig: Config,
      stemmer: Boolean = self.stemmer,
  ): EnglishAnalyzerBuilder =
    new EnglishAnalyzerBuilder(newConfig, stemmer)

  def withConfig(newConfig: Config): EnglishAnalyzerBuilder =
    copy(newConfig = newConfig)

  /** Adds the Porter Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene PorterStemFilter.
    */
  def withPorterStemmer: EnglishAnalyzerBuilder =
    copy(config.copy(lowerCase = true), stemmer = true)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(ts => if (self.stemmer) new PorterStemFilter(ts) else ts)
}

final class FrenchAnalyzerBuilder private[lucene] (
    config: Config,
    stemmer: Boolean,
) extends AnalyzerBuilder(config) { self =>
  type Builder = FrenchAnalyzerBuilder

  private def copy(
      newConfig: Config,
      stemmer: Boolean = self.stemmer,
  ): FrenchAnalyzerBuilder =
    new FrenchAnalyzerBuilder(newConfig, stemmer)

  def withConfig(newConfig: Config): FrenchAnalyzerBuilder =
    copy(newConfig = newConfig)

  /** Adds the FrenchLight Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene FrenchLightStemFilter.
    */
  def withFrenchLightStemmer: FrenchAnalyzerBuilder =
    copy(config.copy(lowerCase = true), stemmer = true)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(ts => if (self.stemmer) new FrenchLightStemFilter(ts) else ts)
}

final class SpanishAnalyzerBuilder private[lucene] (
    config: Config,
    stemmer: Boolean,
) extends AnalyzerBuilder(config) { self =>
  type Builder = SpanishAnalyzerBuilder

  private def copy(
      newConfig: Config,
      stemmer: Boolean = self.stemmer,
  ): SpanishAnalyzerBuilder =
    new SpanishAnalyzerBuilder(newConfig, stemmer)

  def withConfig(newConfig: Config): SpanishAnalyzerBuilder =
    copy(newConfig = newConfig)

  /** Adds the SpanishLight Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene SpanishLightStemFilter.
    */
  def withSpanishLightStemmer: SpanishAnalyzerBuilder =
    copy(config.copy(lowerCase = true), stemmer = true)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(ts => if (self.stemmer) new SpanishLightStemFilter(ts) else ts)
}

final class ItalianAnalyzerBuilder private[lucene] (
    config: Config,
    stemmer: Boolean,
) extends AnalyzerBuilder(config) { self =>
  type Builder = ItalianAnalyzerBuilder

  private def copy(
      newConfig: Config,
      stemmer: Boolean = self.stemmer,
  ): ItalianAnalyzerBuilder =
    new ItalianAnalyzerBuilder(newConfig, stemmer)

  def withConfig(newConfig: Config): ItalianAnalyzerBuilder =
    copy(newConfig = newConfig)

  /** Adds the ItalianLight Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene ItalianLightStemFilter.
    */
  def withItalianLightStemmer: ItalianAnalyzerBuilder =
    copy(config.copy(lowerCase = true), stemmer = true)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(ts => if (self.stemmer) new ItalianLightStemFilter(ts) else ts)
}

final class GermanAnalyzerBuilder private[lucene] (
    config: Config,
    stemmer: Boolean,
) extends AnalyzerBuilder(config) { self =>
  type Builder = GermanAnalyzerBuilder

  private def copy(
      newConfig: Config,
      stemmer: Boolean = self.stemmer,
  ): GermanAnalyzerBuilder =
    new GermanAnalyzerBuilder(newConfig, stemmer)

  def withConfig(newConfig: Config): GermanAnalyzerBuilder =
    copy(newConfig = newConfig)

  /** Adds the GermanLight Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene GermanLightStemFilter.
    */
  def withGermanLightStemmer: GermanAnalyzerBuilder =
    copy(config.copy(lowerCase = true), stemmer = true)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    mkFromStandardTokenizer(config)(ts => if (self.stemmer) new GermanLightStemFilter(ts) else ts)
}
