/*
 * Copyright 2022 CozyDev
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
import cats.syntax.all._
import cats.effect.{IO, IOApp, Resource}
import fs2.{Pipe, Stream}

object MultiLingualPipeline extends IOApp.Simple {

  sealed trait Lang extends Product with Serializable
  case object En extends Lang
  case object Fr extends Lang
  case object Es extends Lang

  case class Msg(lang: Lang, msg: String)
  case class Doc(lang: Lang, tokens: Vector[String])

  val input = Stream(
    Msg(En, "I Like Jalapeños"),
    Msg(En, "Neeko likes jumping on counters"),
    Msg(Fr, "J'aime Les Jalapeños"),
    Msg(Fr, "Neeko aime sauter sur les compteurs"),
    Msg(Es, "Me gustan los jalapeños"),
    Msg(Es, "A Neeko le gusta saltar sobre los mostradores"),
  )

  def multiTokenizer: Resource[IO, Msg => IO[Vector[String]]] = {
    val base = AnalyzerBuilder.default.withLowerCasing.withASCIIFolding.withDefaultStopWords

    val englishA = base.english.withPorterStemmer.tokenizer[IO]
    val frenchA = base.french.withFrenchLightStemmer.tokenizer[IO]
    val spanishA = base.spanish.withSpanishLightStemmer.tokenizer[IO]

    (englishA, frenchA, spanishA).parTupled.map { case (en, fr, es) =>
      (msg: Msg) =>
        msg.lang match {
          case En => en(msg.msg)
          case Fr => fr(msg.msg)
          case Es => es(msg.msg)
        }
    }
  }

  val tokenizeMsgs: Pipe[IO, Msg, Doc] = msgs =>
    Stream
      .resource(multiTokenizer)
      .flatMap(f => msgs.evalMap(m => f(m).map(ts => Doc(m.lang, ts))))

  val docs: Stream[IO, Doc] = input.through(tokenizeMsgs)
  val run = docs.compile.toList.flatMap(IO.println)

}
