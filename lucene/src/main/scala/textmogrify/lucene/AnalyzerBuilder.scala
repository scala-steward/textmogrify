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
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.StopFilter

final class AnalyzerBuilder private (
    val lowerCase: Boolean,
    val foldASCII: Boolean,
    val stopWords: Set[String],
    val stemmer: Boolean,
) { self =>

  private def copy(
      lowerCase: Boolean = self.lowerCase,
      foldASCII: Boolean = self.foldASCII,
      stemmer: Boolean = self.stemmer,
      stopWords: Set[String] = self.stopWords,
  ): AnalyzerBuilder =
    new AnalyzerBuilder(
      lowerCase = lowerCase,
      foldASCII = foldASCII,
      stemmer = stemmer,
      stopWords = stopWords,
    )

  /** Adds a lowercasing stage to the analyzer pipeline */
  def withLowerCasing: AnalyzerBuilder =
    copy(lowerCase = true)

  /** Adds an ASCII folding stage to the analyzer pipeline
    * ASCII folding converts alphanumeric and symbolic Unicode characters into
    * their ASCII equivalents, if one exists.
    */
  def withASCIIFolding: AnalyzerBuilder =
    copy(foldASCII = true)

  /** Adds the Porter Stemmer to the end of the analyzer pipeline and enables lowercasing.
    * Stemming reduces words like `jumping` and `jumps` to their root word `jump`.
    * NOTE: Lowercasing is forced as it is required for the Lucene PorterStemFilter.
    */
  def withPorterStemmer: AnalyzerBuilder =
    copy(stemmer = true, lowerCase = true)

  /** Adds a stop filter stage to analyzer pipeline for non-empty sets.
    */
  def withStopWords(words: Set[String]): AnalyzerBuilder =
    copy(stopWords = words)

  def build[F[_]](implicit F: Sync[F]): Resource[F, Analyzer] =
    Resource.make(F.delay(new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        var tokens = if (self.lowerCase) new LowerCaseFilter(source) else source
        tokens = if (self.foldASCII) new ASCIIFoldingFilter(tokens) else tokens
        tokens =
          if (self.stopWords.isEmpty) tokens
          else {
            val stopSet = new CharArraySet(self.stopWords.size, true)
            stopWords.foreach(w => stopSet.add(w))
            new StopFilter(tokens, stopSet)
          }
        tokens = if (self.stemmer) new PorterStemFilter(tokens) else tokens
        new TokenStreamComponents(source, tokens)
      }
    }))(analyzer => F.delay(analyzer.close()))

  def tokenizer[F[_]](implicit F: Sync[F]): Resource[F, String => F[Vector[String]]] =
    self.build.map(a => Tokenizer.vectorTokenizer(a))
}
object AnalyzerBuilder {
  def default: AnalyzerBuilder = new AnalyzerBuilder(
    lowerCase = false,
    foldASCII = false,
    stemmer = false,
    stopWords = Set.empty,
  )
}
