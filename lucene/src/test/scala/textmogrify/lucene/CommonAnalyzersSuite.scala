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
import cats.effect.IO

class CommonAnalyzersSuite extends CatsEffectSuite {

  test("asciiFolder should fold") {
    val an = CommonAnalyzers.asciiFolder[IO]
    val tokenizer = an.map(a => Tokenizer.vectorTokenizer[IO](a))
    val actual = tokenizer.use { f =>
      f("I like jalapeños")
    }
    assertIO(actual, Vector("I", "like", "jalapenos"))
  }

}
