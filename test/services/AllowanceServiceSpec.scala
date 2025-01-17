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

package services

import controllers.auth.AuthenticatedRequest
import models.AtsData
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.MustMatchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import services.atsData.AtsTestData
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.GenericViewModel
import utils.TestConstants._
import view_models._

import scala.concurrent.Await
import scala.concurrent.duration._

class AllowanceServiceSpec extends UnitSpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar {

  val genericViewModel: GenericViewModel = AtsList(
    utr = "3000024376",
    forename = "forename",
    surname = "surname",
    yearList = List(
      TaxYearEnd(Some("2015"))
    )
  )

  val noAtsaViewModel: NoATSViewModel = new NoATSViewModel()

  implicit val hc = HeaderCarrier()

  val request = AuthenticatedRequest(
    "userId",
    None,
    Some(SaUtr(testUtr)),
    None,
    None,
    None,
    None,
    true,
    fakeCredentials,
    FakeRequest("GET", s"?taxYear=${sut.taxYear}"))

  val mockAtsService: AtsService = mock[AtsService]
  val mockAtsYearListService: AtsYearListService = mock[AtsYearListService]

  def sut = new AllowanceService(mockAtsService, mockAtsYearListService) with MockitoSugar {
    implicit val hc = new HeaderCarrier
    val taxYear = 2015

  }

  "AllowanceService.getAllowances" should {

    "return a GenericViewModel when TaxYearUtil.extractTaxYear returns a taxYear" in {
      when(
        mockAtsService.createModel(Matchers.eq(sut.taxYear), Matchers.any[Function1[AtsData, GenericViewModel]]())(
          Matchers.any(),
          Matchers.any())).thenReturn(genericViewModel)
      val result = Await.result(sut.getAllowances(sut.taxYear)(request, hc), 1500 millis)
      result mustEqual genericViewModel
    }
  }

  "AllowanceService.allowanceDataConverter" should {
    "return a complete AllowancesData when given complete AtsData" in {

      val atsData = AtsTestData.atsAllowancesData
      val result = sut.allowanceDataConverter(atsData)

      result shouldBe Allowances(
        2019,
        "1111111111",
        Amount(100, "GBP"),
        Amount(200, "GBP"),
        Amount(300, "GBP"),
        Amount(400, "GBP"),
        "Mr",
        "John",
        "Smith"
      )
    }
  }
}
