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

import org.scalatest.OptionValues
import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, _}
import play.modules.reactivemongo.ReactiveMongoComponent
import util.RequestHeaders._
import util.TestData.HandleSubscription._
import util.TestData._
import util._
import util.mongo.ReactiveMongoComponentForTests

import scala.concurrent.Future

class HandleSubscriptionSpec extends ComponentTestSpec
  with OptionValues
  with TaxEnrolmentService
  with TableDrivenPropertyChecks {

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> false)
    .configure(Map(
      "microservice.services.tax-enrolments.host" -> ExternalServicesConfig.Host,
      "microservice.services.tax-enrolments.port" -> ExternalServicesConfig.Port,
      "play.filters.csrf.header.bypassHeaders.Authorization" -> "*"
    )
    )
    .overrides(bind[ReactiveMongoComponent].to[ReactiveMongoComponentForTests])
    .build()

  override def beforeAll: Unit = {
    startMockServer()
    returnEnrolmentResponseWhenReceiveRequest(s"/tax-enrolments/subscriptions/$formBundleId/subscriber",
      TaxEnrolment.validRequestJsonString, NO_CONTENT)
  }

  override protected def afterAll() {
    stopMockServer()
  }

  override protected def beforeEach(): Unit = {
    dropDatabase()
  }

  override protected def afterEach(): Unit = {
    resetMockServer()
  }

  feature("Ensure connection to the API is available") {

    scenario("Customs Frontend submits a valid request to handle subscription using the API") {

      Given("the API is available")

      When("a POST request with data is sent to the API")
      val result = route(app, request())

      Then("a response with a 204 status is received")
      val Some(resultFuture) = result

      status(resultFuture) shouldBe NO_CONTENT
      verifyTaxEnrolmentsCalled()

      And("the response body is empty")
      contentAsString(resultFuture) shouldBe empty
    }

  }

  feature("Ensure API handles invalid requests") {

    val table1 =
      Table(
        ("description", "request", "expected response code", "expected JSON result")
        , ("content type header missing", requestWithTextBody(headers = validHeaders - CONTENT_TYPE), UNSUPPORTED_MEDIA_TYPE, errorUnsupportedMediaType)
        , ("content type header invalid", requestWithTextBody(headers = validHeaders + CONTENT_TYPE_HEADER_INVALID), UNSUPPORTED_MEDIA_TYPE, errorUnsupportedMediaType)
        , ("accept header missing", requestWithTextBody(headers = validHeaders - ACCEPT - CONTENT_TYPE), NOT_ACCEPTABLE, errorAcceptHeaderInvalid)
        , ("accept header invalid", requestWithTextBody(headers = validHeaders + ACCEPT_HEADER_INVALID - CONTENT_TYPE), NOT_ACCEPTABLE, errorAcceptHeaderInvalid)
      )

    forAll(table1) { (description, request, httpCode, responseAsJson) =>
      scenario(s"Customs Frontend submits an invalid request with $description") {

        Given("the API is available")

        When("an invalid POST request is sent to the API")
        val result: Option[Future[Result]] = route(app, request)

        Then(s"a response with a $httpCode status is received")
        result shouldBe 'defined
        val resultFuture: Future[Result] = result.value

        status(resultFuture) shouldBe httpCode

        And("the response body contains error")
        contentAsJson(resultFuture) shouldBe responseAsJson
      }

    }

    val table2 =
      Table(
        ("description", "request", "expected response code", "expected JSON result")
        , ("authorization header missing", request(headers = validHeaders - AUTHORIZATION), UNAUTHORIZED, errorUnauthorized)
        , ("authorization header invalid", request(headers = validHeaders + AUTHORISATION_HEADER_INVALID), UNAUTHORIZED, errorUnauthorized)
        , ("invalid JSON payload", request(body = Json.parse("{}")), BAD_REQUEST, errorPayloadInvalid)
      )

    forAll(table2) { (description, request, httpCode, responseAsJson) =>
      scenario(s"Customs Frontend submits an invalid request with $description") {

        Given("the API is available")

        When("an invalid POST request is sent to the API")
        val result: Option[Future[Result]] = route(app, request)

        Then(s"a response with a $httpCode status is received")
        result shouldBe 'defined
        val resultFuture: Future[Result] = result.value

        status(resultFuture) shouldBe httpCode

        And("the response body contains error")
        contentAsJson(resultFuture) shouldBe responseAsJson
      }

    }

  }
}
