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
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.customs.managesubscription.repository.RecipientDetailsRepository
import util.RequestHeaders._
import util.TestData.SubscriptionResult._
import util.TestData._
import util.mongo.ReactiveMongoComponentForTests
import util.{ExternalServicesConfig, MDTPEmailService}

import scala.concurrent.Future


class SubscriptionResultSpec extends ComponentTestSpec
  with OptionValues
  with MDTPEmailService {

  private lazy val recipientDetailsRepository = app.injector.instanceOf[RecipientDetailsRepository]

  implicit override lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.email.host" -> ExternalServicesConfig.Host,
    "microservice.services.email.port" -> ExternalServicesConfig.Port,
    "microservice.services.customs-data-store.host" -> ExternalServicesConfig.Host,
    "microservice.services.customs-data-store.port" -> ExternalServicesConfig.Port,
    "play.filters.csrf.header.bypassHeaders.Authorization" -> "*"

  )
  ).overrides(bind[ReactiveMongoComponent].to[ReactiveMongoComponentForTests])
    .build()

  override def beforeAll: Unit = {
    startMockServer()
    mdtpEmailServiceIsRunning()
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
    scenario("MDG submits a request with data using the API") {

      Given("the API is available")
      await(recipientDetailsRepository.saveRecipientDetailsForBundleId(formBundleId, Some(eori), recipientDetails, emailVerificationTimestamp, safeId)) shouldBe true
      val request = FakeRequest("POST", s"/$formBundleId")
        .withHeaders(CONTENT_TYPE_HEADER, ACCEPT_HEADER, AUTHORISATION_HEADER)

      When("a POST request with data is sent to the API")
      val result: Option[Future[Result]] = route(app, request.withJsonBody(validSucceededJsonBody))

      Then("a response with a 204 status is received")
      result shouldBe 'defined
      val resultFuture: Future[Result] = result.value

      status(resultFuture) shouldBe NO_CONTENT

      And("the response body is empty")
      contentAsString(resultFuture) shouldBe 'empty
    }
  }
}
