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

import textmogrify.lucene.AnalyzerBuilder
import cats.effect.{IO, IOApp}
import fs2.{Pipe, Stream}

object Pipeline extends IOApp.Simple {

  case class Msg(id: Int, msg: String)
  case class Doc(id: Int, tokens: Vector[String])

  val input = Stream(
    Msg(0, "How do i trim my cats nails?"),
    Msg(1, "trimming cat nail"),
    Msg(2, "cat scratching furniture"),
  )

  val tokenizeMsgs: Pipe[IO, Msg, Doc] = msgs => {
    val tokenizer = AnalyzerBuilder.default.withLowerCasing
      .withStopWords(Set("how", "do", "i", "my"))
      .withPorterStemmer
      .tokenizer[IO]
    Stream
      .resource(tokenizer)
      .flatMap(f => msgs.evalMap(m => f(m.msg).map(ts => Doc(m.id, ts))))
  }

  val docs: Stream[IO, Doc] = input.through(tokenizeMsgs)
  val run = docs.compile.toList.flatMap(IO.println)

}
