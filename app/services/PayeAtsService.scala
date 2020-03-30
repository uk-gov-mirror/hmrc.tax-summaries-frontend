/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import java.util.Date

import connectors.MiddleConnector
import controllers.auth.PayeAuthenticatedRequest
import models.PayeAtsData
import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, NotFoundException}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PayeAtsService extends PayeAtsService{
  override val middleConnector = MiddleConnector
  override val auditService = AuditService
}

trait PayeAtsService {
  def middleConnector: MiddleConnector
  def auditService: AuditService

  def getPayeATSData(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier, request: PayeAuthenticatedRequest[_]):Future[Either[HttpResponse,PayeAtsData]] = {
     middleConnector.connectToPayeATS(nino,taxYear) map { response =>
       response status match {
         case OK =>  {
           sendAuditEvent(taxYear)
           Right(response.json.as[PayeAtsData])
         }
         case _ => Left(response)
       }
     } recover {
       case e: BadRequestException => Left(HttpResponse(BAD_REQUEST))
       case e: NotFoundException   => Left(HttpResponse(NOT_FOUND))
       case e: Exception => {
         Logger.error(s"Exception in PayeAtsService: $e", e)
         Left(HttpResponse(INTERNAL_SERVER_ERROR))
       }
     }
  }

  private def sendAuditEvent(taxYear: Int)(implicit hc: HeaderCarrier, request: PayeAuthenticatedRequest[_]) = {
    auditService.sendEvent(
      auditType = AuditTypes.Tx_SUCCEEDED,
      details = Map(
        "userId" -> request.userId,
        "userNino" -> request.nino.nino,
        "taxYear" -> taxYear.toString,
        "time" -> new Date().toString
      )
    )
  }
}
