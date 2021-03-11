/*
 * Copyright 2021 HM Revenue & Customs
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

package component

import java.util.concurrent.TimeUnit

import org.scalatest._
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import util.mongo.ReactiveMongoComponentForTests
import util.{AsyncTest, WireMockRunner}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


trait ComponentTestSpec extends FeatureSpec with Matchers with GivenWhenThen with GuiceOneAppPerSuite
  with WireMockRunner with AsyncTest with BeforeAndAfterAll with BeforeAndAfterEach {

  val defaultTimeout: Int = 5

  implicit override val patienceConfig = PatienceConfig(timeout = scaled(Span(defaultTimeout, Seconds)),
    interval = scaled(Span(defaultTimeout, Millis)))

  def await[A](future: Future[A]): A = Await.result(future, Duration(5, TimeUnit.SECONDS))

  def dropDatabase(): Unit = {
    await(new ReactiveMongoComponentForTests(app, Environment.simple()).mongoConnector.db().drop())
  }
}
