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

import cats.effect.kernel.{Resource, Sync}
import org.apache.lucene.analysis.Analyzer

object AnalyzerResource {

  /** Wrap an Analyzer in a Resource
    */
  def fromAnalyzer[F[_]](analyzer: => Analyzer)(implicit F: Sync[F]): Resource[F, Analyzer] =
    Resource.make(F.delay(analyzer))(analyzer => F.delay(analyzer.close()))

  /** Construct a tokenizing function directly from an Analyzer
    */
  def tokenizer[F[_]](
      analyzer: => Analyzer
  )(implicit F: Sync[F]): Resource[F, String => F[Vector[String]]] =
    fromAnalyzer(analyzer)
      .map(a => Tokenizer.vectorTokenizer(a))
}
