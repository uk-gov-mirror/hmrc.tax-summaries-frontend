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
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.i18n.Messages
import play.api.test.Helpers._
import services._
import utils.TestConstants._
import view_models._

import scala.concurrent.Future
import scala.math.BigDecimal.double2bigDecimal

object SummaryControllerSpec {

  val baseModel = Summary(
    year = 2014,
    utr = testUtr,
    employeeNicAmount = Amount(1200, "GBP"),
    totalIncomeTaxAndNics = Amount(1400, "GBP"),
    yourTotalTax = Amount(1800, "GBP"),
    totalTaxFree = Amount(9440, "GBP"),
    totalTaxFreeAllowance = Amount(9740, "GBP"),
    yourIncomeBeforeTax = Amount(11600, "GBP"),
    totalIncomeTaxAmount = Amount(372, "GBP"),
    totalCapitalGainsTax = Amount(5500, "GBP"),
    taxableGains = Amount(20000, "GBP"),
    cgTaxPerCurrencyUnit = Amount(0.1234, "GBP"),
    nicsAndTaxPerCurrencyUnit = Amount(0.5678, "GBP"),
    totalCgTaxRate = Rate("12.34%"),
    nicsAndTaxRate = Rate("56.78%"),
    title = "Mr",
    forename = "forename",
    surname = "surname"
  )
}

class SummaryControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach with ScalaCheckDrivenPropertyChecks {

  override val taxYear = 2014

  val baseModel = SummaryControllerSpec.baseModel

  val mockSummaryService = mock[SummaryService]
  val mockAuditService = mock[AuditService]

  def sut =
    new SummaryController(
      mockSummaryService,
      mockAuditService,
      FakeAuthAction,
      mcc,
      summaryView,
      genericErrorView,
      tokenErrorView)

  override def beforeEach(): Unit =
    when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
      .thenReturn(Future.successful(baseModel))

  "Calling Summary" should {

    "return a successful response for a valid request" in {
      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))
      document.title should include(
        Messages("ats.summary.title") + Messages("generic.to_from", (taxYear - 1).toString, taxYear.toString))
    }

    "display an error page for an invalid request" in {
      val result = Future.successful(sut.show(badRequest))
      status(result) shouldBe 400
      val document = Jsoup.parse(contentAsString(result))
      document.title should include(Messages("global.error.InternalServerError500.title"))
    }

    "display an error page when AtsUnavailableViewModel is returned" in {

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(new ATSUnavailableViewModel))

      val result = Future.successful(sut.show(request))
      status(result) mustBe INTERNAL_SERVER_ERROR

      val document = Jsoup.parse(contentAsString(result))
      document.title should include(Messages("global.error.InternalServerError500.title"))
    }

    "redirect to the no ATS page when there is no Annual Tax Summary data returned" in {
      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(new NoATSViewModel))
      val result = Future.successful(sut.show(request))
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.ErrorController.authorisedNoAts().url
    }

    "have the right user data in the view" in {

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.toString contains "Tax calculation"
      document.getElementById("income-before-tax-amount").text() shouldBe "£11,600"
      document.getElementById("user-info").text should include("forename surname")
      document.getElementById("user-info").text should include("Unique Taxpayer Reference: " + testUtr)
    }

    "have the correct tax free amount" in {

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-free-amount").text() shouldBe "£9,740"
    }

    "still show a 0 tax free amount" in {

      val model2 = baseModel.copy(
        totalTaxFreeAllowance = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model2))

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-free-amount").text() shouldBe "£0"
    }

    "show total income tax and NICs value on the summary page" in {

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-income-tax-and-nics").text() shouldBe "£1,572"
    }

    "show capital gains (and description) on the summary if capital gains is not 0" in {

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("capital-gains") should not be null
    }

    "not show capital gains on the summary if capital gains is 0" in {

      val model3 = baseModel.copy(
        taxableGains = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model3))

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.toString should not include ("Technical Difficulties")
      document.getElementById("capital-gains") should be(null)
    }

    "show Total Capital Gains Tax value" in {

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-capital-gains-tax").text() should equal("£5,500")
    }

    "show capital gains description on the summary if total capital gains tax is not 0" in {

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-cg-description") should not be null
    }

    "hide capital gains description on the summary if total capital gains tax is  0" in {

      val model4 = baseModel.copy(
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model4))

      val result = Future.successful(sut.show(request))
      status(result) shouldBe 200
      val document = Jsoup.parse(contentAsString(result))

      document.toString should not include ("Technical Difficulties")
      document.getElementById("total-cg-description") should be(null)
    }

    "show only NICs in Total Income Tax value" when {

      "Income tax is zero or less" in {

        forAll { bd: BigDecimal =>
          whenever(bd <= 0) {

            val model5 = baseModel.copy(
              totalIncomeTaxAmount = Amount(bd, "GBP")
            )

            when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
              .thenReturn(Future.successful(model5))

            val result = Future.successful(sut.show(request))
            status(result) shouldBe 200
            val document = Jsoup.parse(contentAsString(result))

            document.getElementById("total-income-tax-and-nics").text() should equal("£1,200")
          }
        }
      }
    }

    "show Tax and Nics description having (income tax and employee nics)" in {

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Income Tax and National Insurance")
    }

    "show Tax and Nics description having only (total income tax)" in {

      val model6 = baseModel.copy(
        employeeNicAmount = Amount(0, "GBP"),
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model6))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Your tax was calculated as")
    }

    "show Tax and Nics description having only (capital gains)" in {

      val model7 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP"),
        employeeNicAmount = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model7))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Your tax was calculated as")
    }

    "show Tax and Nics description having only (employee nics)" in {

      val model8 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP"),
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model8))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Your NICs were calculated as")
    }

    "show Tax and Nics description having only (total income tax, employee nics)" in {

      val model9 = baseModel.copy(
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model9))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Income Tax and National Insurance")
    }

    "show Tax and Nics description having only (capital gains, employee nics)" in {

      val model10 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model10))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("tax-and-nics-title").text() should equal("Your NICs were calculated as")
    }

    "show Your Total Tax as sum of Income Tax, capital gains and employee nics)" in {

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-amount").text() shouldBe "£1,800"
    }

    "show Your Total Tax description having (total income tax, capital gains, employee nics)" in {

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(baseModel))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal(
        "Your total Income Tax, National Insurance and Capital Gains Tax.")
    }

    "show Your Total Tax description having only (total income tax)" in {

      val model11 = baseModel.copy(
        employeeNicAmount = Amount(0, "GBP"),
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model11))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal("Your total Income Tax.")
    }

    "show Your Total Tax description having only (capital gains)" in {

      val model12 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP"),
        employeeNicAmount = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model12))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal("Your Capital Gains Tax.")
    }

    "show Your Total Tax description having only (employee nics)" in {

      val model13 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP"),
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model13))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal("Your National Insurance.")
    }

    "show Your Total Tax description having only (total income tax, capital gains)" in {

      val model14 = baseModel.copy(
        employeeNicAmount = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model14))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal(
        "Your total Income Tax and Capital Gains Tax.")
    }

    "show Your Total Tax description having only (total income tax, employee nics)" in {

      val model15 = baseModel.copy(
        totalCapitalGainsTax = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model15))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal(
        "Your total Income Tax and National Insurance.")
    }

    "show Your Total Tax description having only (capital gains, employee nics)" in {

      val model16 = baseModel.copy(
        totalIncomeTaxAmount = Amount(0, "GBP")
      )

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model16))

      val result = Future.successful(sut.show(request))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementById("total-tax-description").text() should equal(
        "Your National Insurance and Capital Gains Tax.")
    }

    "Redirect to 'No ATS' page" in {

      val model17 = new NoATSViewModel

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request)))
        .thenReturn(Future.successful(model17))

      val result = Future.successful(sut.show(request))

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
    }
  }
}
