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
    new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        val tokens = new LowerCaseFilter(source)
        new TokenStreamComponents(source, new PorterStemFilter(tokens))
      }
    }

  def asciiFolder(): Analyzer =
    new Analyzer {
      protected def createComponents(fieldName: String): TokenStreamComponents = {
        val source = new StandardTokenizer()
        new TokenStreamComponents(source, new ASCIIFoldingFilter(source))
      }
    }

}
