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

package controllers

import controllers.auth.FakeAuthAction
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.MustMatchers._
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.test.Helpers._
import services._
import uk.gov.hmrc.http.HeaderCarrier
import utils.GenericViewModel
import utils.TestConstants._
import view_models._

import scala.concurrent.Future

class AllowancesControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  override val taxYear = 2014

  val baseModel = Allowances(
    taxYear = 2014,
    utr = testUtr,
    taxFreeAllowance = Amount(9440, "GBP"),
    marriageAllowanceTransferred = Amount(0, "GBP"),
    otherAllowances = Amount(300, "GBP"),
    totalTaxFree = Amount(9740, "GBP"),
    title = "Mr",
    forename = "forename",
    surname = "surname"
  )

  val genericViewModel: GenericViewModel = AtsList(
    utr = "3000024376",
    forename = "forename",
    surname = "surname",
    yearList = List(
      TaxYearEnd(Some("2015"))
    )
  )

  implicit val hc = new HeaderCarrier

  val noATSViewModel: NoATSViewModel = new NoATSViewModel()

  lazy val taxsController = mock[TaxsController]

  val mockAllowanceService = mock[AllowanceService]
  val mockAuditService = mock[AuditService]

  def sut =
    new AllowancesController(
      mockAllowanceService,
      mockAuditService,
      FakeAuthAction,
      mcc,
      taxFreeAmountView,
      genericErrorView,
      tokenErrorView)

  override def beforeEach(): Unit =
    when(mockAllowanceService.getAllowances(Matchers.eq(taxYear))(Matchers.eq(request), Matchers.any())) thenReturn Future
      .successful(baseModel)

  "Calling allowances" should {

    "have the right user data in the view when a valid request is sent" in {

      val result = Future.successful(sut.show(request))

      status(result) shouldBe 200

      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-free-total").text() shouldBe "£9,740"
      document.getElementById("tax-free-allowance-amount").text() shouldBe "£9,440"
      document.getElementById("other-allowances").text() shouldBe "£300"
      document.toString should include("tax-free-allowance")
      document.getElementById("user-info").text() should include("forename surname")
      document.getElementById("user-info").text() should include("Unique Taxpayer Reference: " + testUtr)
      document.select("h1").text shouldBe "Tax year: April 6 2013 to April 5 2014 Your tax-free amount"
    }

    "have zero-value fields hidden in the view" in {

      val model = baseModel.copy(
        taxFreeAllowance = Amount(0, "GBP"),
        otherAllowances = Amount(0, "GBP")
      )

      when(mockAllowanceService.getAllowances(Matchers.eq(taxYear))(Matchers.eq(request), Matchers.any()))
        .thenReturn(Future.successful(model))

      val result: Future[Result] = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.toString should not include "tax-free-allowance-amount"
      document.toString should not include "other-allowances"
    }

    "return a successful response for a valid request" in {
      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))
      document.title should include(
        Messages("ats.tax_free_amount.html.title") + Messages(
          "generic.to_from",
          (taxYear - 1).toString,
          taxYear.toString))
    }

    "display an error page for an invalid request" in {
      val result = Future.successful(sut.show(badRequest))
      status(result) shouldBe 400
      val document = Jsoup.parse(contentAsString(result))
      document.title should include(Messages("global.error.InternalServerError500.title"))
    }

    "display an error page when AtsUnavailableViewModel is returned" in {

      when(mockAllowanceService.getAllowances(Matchers.eq(taxYear))(Matchers.eq(request), Matchers.any()))
        .thenReturn(Future.successful(new ATSUnavailableViewModel))

      val result = Future.successful(sut.show(request))
      status(result) mustBe INTERNAL_SERVER_ERROR

      val document = Jsoup.parse(contentAsString(result))
      document.title should include(Messages("global.error.InternalServerError500.title"))
    }

    "redirect to the no ATS page when there is no Annual Tax Summary data returned" in {
      when(mockAllowanceService.getAllowances(Matchers.eq(taxYear))(Matchers.eq(request), Matchers.any()))
        .thenReturn(Future.successful(new NoATSViewModel))
      val result = Future.successful(sut.show(request))
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.ErrorController.authorisedNoAts().url
    }

  }
}
