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

import javax.inject.Inject
import play.api.http.HeaderNames._
import play.api.mvc._
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.customs.managesubscription.controllers.ErrorResponse._

import scala.concurrent.{ExecutionContext, Future}

class DigitalHeaderValidator @Inject()(mcc: MessagesControllerComponents) extends ActionBuilder[Request, AnyContent] {

  override val executionContext: ExecutionContext = mcc.executionContext
  override val parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  private val BearerTokenRegex = "^Bearer .*"

  def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    implicit val headers: Headers = request.headers
    if (!accept) {
      Future.successful(ErrorAcceptHeaderInvalid.JsonResult)
    } else if (!contentType) {
      Future.successful(ErrorContentTypeHeaderInvalid.JsonResult)
    } else if (!bearerToken) {
      Future.successful(ErrorUnauthorized.JsonResult)
    } else {
      block(request)
    }
  }

  private def accept(implicit h: Headers) = h.get(ACCEPT).fold(false)(_ == "application/vnd.hmrc.1.0+json")

  private def contentType(implicit h: Headers) = h.get(CONTENT_TYPE).fold(false)(_ == MimeTypes.JSON)

  private def bearerToken(implicit h: Headers) = h.get(AUTHORIZATION).fold(false)(_.matches(BearerTokenRegex))
}
