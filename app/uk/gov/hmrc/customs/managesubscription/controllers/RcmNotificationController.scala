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

package uk.gov.hmrc.customs.managesubscription.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisedFunctions}
import uk.gov.hmrc.customs.managesubscription.connectors.MicroserviceAuthConnector
import uk.gov.hmrc.customs.managesubscription.domain.RcmNotificationRequest
import uk.gov.hmrc.customs.managesubscription.services.EmailService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RcmNotificationController @Inject()(emailService: EmailService,
                                          cc: ControllerComponents,
                                          override val authConnector: MicroserviceAuthConnector)
                                          extends BackendController(cc) with AuthorisedFunctions  {

  def notifyRCM(): Action[AnyContent] = Action async {
    implicit request =>
      authorised(AuthProviders(GovernmentGateway)) {
        request.body.asJson.fold(ifEmpty = Future.successful(ErrorResponse.ErrorGenericBadRequest.JsonResult)) { js =>
          js.validate[RcmNotificationRequest] match {
            case JsSuccess(r, _) =>
              emailService.sendEmail(r).map(_ => NoContent)
            case JsError(_) =>
              Future.successful(ErrorResponse.ErrorInvalidPayload.JsonResult)
          }
        }
      }
  }
}
