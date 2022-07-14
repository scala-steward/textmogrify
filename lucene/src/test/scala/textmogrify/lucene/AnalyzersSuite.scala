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

class AnalyzersSuite extends CatsEffectSuite {

  test("asciiFolder should fold") {
    val tokenizer = AnalyzerResource.tokenizer[IO](Analyzers.asciiFolder())
    val actual = tokenizer.use { f =>
      f("I like jalapeños")
    }
    assertIO(actual, Vector("I", "like", "jalapenos"))
  }

  test("asciiFolderLower should fold and lowercase") {
    val tokenizer = AnalyzerResource.tokenizer[IO](Analyzers.asciiFolderWithLower())
    val actual = tokenizer.use { f =>
      f("I Like Jalapeños")
    }
    assertIO(actual, Vector("i", "like", "jalapenos"))
  }

  test("analyzer with stopWords should filter them out") {
    val analyzer = AnalyzerBuilder.default.withStopWords(Set("I")).build
    val tokenizer = AnalyzerResource.tokenizer[IO](analyzer)
    val actual = tokenizer.use { f =>
      f("I Like Jalapeños")
    }
    assertIO(actual, Vector("Like", "Jalapeños"))
  }

}
