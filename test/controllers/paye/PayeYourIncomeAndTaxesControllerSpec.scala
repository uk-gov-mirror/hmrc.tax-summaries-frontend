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

package controllers.paye

import controllers.auth.{FakePayeAuthAction, PayeAuthenticatedRequest}
import models.PayeAtsData
import org.jsoup.Jsoup
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.i18n.Messages
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import services.atsData.PayeAtsTestData
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.TestConstants.testNino
import views.html.paye.PayeYourIncomeAndTaxesView

class PayeYourIncomeAndTaxesControllerSpec extends PayeControllerSpecHelpers {

  val fakeAuthenticatedRequest = buildPayeRequest("/annual-tax-summary/paye/treasury-spending")

  val sut = new PayeYourIncomeAndTaxesController(
    mockPayeAtsService,
    FakePayeAuthAction,
    mcc,
    inject[PayeYourIncomeAndTaxesView])

  "Government spend controller" should {

    "return OK response" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(taxYear))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Right(expectedResponse.as[PayeAtsData]))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe OK

      val document = Jsoup.parse(contentAsString(result))

      document.title should include(
        Messages("paye.ats.summary.title") + Messages("generic.to_from", (taxYear - 1).toString, taxYear.toString))
    }

    "return 200 when total_income_before_tax key is missing in PAYE ATS data" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(2019))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Right(PayeAtsTestData.malformedYourIncomeAndTaxesData))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe OK
    }

    "throw internal server exception when summary data is missing" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(taxYear))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Right(PayeAtsTestData.missingYourIncomeAndTaxesData))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe INTERNAL_SERVER_ERROR

      contentAsString(result) shouldBe "Missing Paye ATS data"
    }

    "redirect user to noAts page when receiving NOT_FOUND from service" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(taxYear))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Left(HttpResponse(NOT_FOUND, "")))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.PayeErrorController.authorisedNoAts().url
    }

    "show Generic Error page and return INTERNAL_SERVER_ERROR when receiving BAD_REQUEST from service" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(taxYear))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Left(HttpResponse(BAD_REQUEST, "")))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.PayeErrorController.genericError(BAD_REQUEST).url)
    }

    "show Generic Error page and return INTERNAL_SERVER_ERROR if error received from service" in {

      when(
        mockPayeAtsService
          .getPayeATSData(eqTo(testNino), eqTo(taxYear))(any[HeaderCarrier], any[PayeAuthenticatedRequest[_]]))
        .thenReturn(Left(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result = sut.show(taxYear)(fakeAuthenticatedRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.PayeErrorController.genericError(INTERNAL_SERVER_ERROR).url)
    }
  }

}
