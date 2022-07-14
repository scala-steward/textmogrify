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

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter

object Analyzers {

  def englishStandard(): Analyzer = new EnglishAnalyzer()

  def porterStemmer(): Analyzer =
    AnalyzerBuilder.default.withLowerCasing.withPorterStemmer.build

  def asciiFolder(): Analyzer =
    AnalyzerBuilder.default.withASCIIFolding.build

  def asciiFolderWithLower(): Analyzer =
    AnalyzerBuilder.default.withLowerCasing.withASCIIFolding.build

}

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

  def withLowerCasing: AnalyzerBuilder =
    copy(lowerCase = true)

  def withASCIIFolding: AnalyzerBuilder =
    copy(foldASCII = true)

  def withPorterStemmer: AnalyzerBuilder =
    copy(stemmer = true)

  def withStopWords(words: Set[String]): AnalyzerBuilder =
    copy(stopWords = words)

  def build: Analyzer =
    new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        var tokens = if (self.lowerCase) new LowerCaseFilter(source) else source
        tokens = if (self.foldASCII) new ASCIIFoldingFilter(tokens) else tokens
        tokens = if (self.stemmer) new PorterStemFilter(tokens) else tokens
        new TokenStreamComponents(source, tokens)
      }
    }
}
object AnalyzerBuilder {
  def default: AnalyzerBuilder = new AnalyzerBuilder(
    lowerCase = false,
    foldASCII = false,
    stemmer = false,
    stopWords = Set.empty,
  )
}
