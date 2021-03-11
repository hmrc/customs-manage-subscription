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

package uk.gov.hmrc.customs.managesubscription.connectors


import javax.inject.{Inject, Singleton}
import play.api.http.Status.{ACCEPTED, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.customs.managesubscription.CdsLogger.logger
import uk.gov.hmrc.customs.managesubscription.audit.Auditable
import uk.gov.hmrc.customs.managesubscription.config.AppConfig
import uk.gov.hmrc.customs.managesubscription.services.dto.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmailConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, audit: Auditable) {


  def sendEmail(email: Email)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    httpClient.doPost[Email](appConfig.emailServiceUrl, email, Seq("Content-Type" -> "application/json"))
      .map { response =>
        audit(email,response,appConfig.emailServiceUrl)
        logResponse(email.templateId)(response.status); response
      }
  }

  private def logResponse(templateId: String): Int => Unit = {
    case ACCEPTED | OK => logger.info(s"sendEmail succeeded for template Id: $templateId")
    case status => logger.info(s"sendEmail: request is failed with status $status for template Id: $templateId")
  }

  private def audit(email: Email, response: HttpResponse, url: String)(implicit hc: HeaderCarrier): Future[Unit] =
   Future.successful {
     audit.sendDataEvent(
       transactionName = "EmailRequestSubmitted",
       path = url,
       detail = Map("emailRequest" ->  Json.prettyPrint(Json.toJson(email)), "status" -> response.status.toString),
       auditType = "customs-email-status"
     )
   }
}