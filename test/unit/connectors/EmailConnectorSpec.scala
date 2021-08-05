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

package unit.connectors

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, verify, when}
import play.api.libs.json.Json
import uk.gov.hmrc.customs.managesubscription.audit.Auditable
import uk.gov.hmrc.customs.managesubscription.connectors.EmailConnector
import uk.gov.hmrc.customs.managesubscription.services.dto.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import util.BaseSpec

import scala.concurrent.Future

class EmailConnectorSpec extends BaseSpec {
  val mockHttp = mock[HttpClient]
  val mockAuditable = mock[Auditable]
  implicit val hc = new HeaderCarrier()
  val testConnector = new EmailConnector(appConfig, mockHttp, mockAuditable)
  val emailRequest = Email(List("toEmail"), "templateId", Map.empty)

  private val transactionName = "EmailRequestSubmitted"
  private val path = "http://localhost:8300/hmrc/email"
  private val details = Map("emailRequest" -> Json.prettyPrint(Json.toJson(emailRequest)),"status" -> "200")
  private val auditType = "customs-email-status"
  "EmailConnector" should {
    "successfully send a email request to Email service and return the OK response" in {
      when(mockHttp.POST[Email, HttpResponse](any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(HttpResponse(200, "")))
      doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      val result = await(testConnector.sendEmail(emailRequest))
      result.status shouldBe 200
      verify(mockAuditable).sendDataEvent(transactionName, path, details, auditType)
    }

    "successfully send a email request to Email service and return the ACCEPTED response" in {
      val details202 = Map("emailRequest" -> Json.prettyPrint(Json.toJson(emailRequest)),"status" -> "202")
      when(mockHttp.POST[Email, HttpResponse](any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(HttpResponse(202, "")))
      val result = await(testConnector.sendEmail(Email(List("toEmail"), "templateId", Map.empty)))
      result.status shouldBe 202
      verify(mockAuditable).sendDataEvent(transactionName, path, details202, auditType)
    }

    "return the failure response from Email service" in {
      when(mockHttp.POST[Email, HttpResponse](any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(HttpResponse(400, "")))
      val emailRequest = Email(List(""), "templateId", Map.empty)
      val details400 = Map("emailRequest" -> Json.prettyPrint(Json.toJson(emailRequest)),"status" -> "400")
      val result = await(testConnector.sendEmail(Email(List(""), "templateId", Map.empty)))
      result.status shouldBe 400
      verify(mockAuditable).sendDataEvent(transactionName, path, details400, auditType)

    }
  }
}
