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

package controllers

import controllers.auth.{AuthAction, AuthenticatedRequest, FakeAuthAction}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, _}
import services._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.test.UnitSpec
import view_models.NoATSViewModel
import utils.TestConstants._

import scala.concurrent.Future

class ZeroTaxLiabilitySpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  val taxYear = 2015
  val request = AuthenticatedRequest("userId", None, Some(SaUtr(testUtr)), None, None, None, None, FakeRequest("GET", s"?taxYear=$taxYear"))
  val dataPath = "/no_ats_json_test.json"
  val model = new NoATSViewModel

  val mockIncomeService = mock[IncomeService]
  val mockAuditService = mock[AuditService]
  val mockSummaryService = mock[SummaryService]

  def incomeController = new IncomeController(mockIncomeService) {
    override val auditService = mock[AuditService]
    override val authAction: AuthAction = FakeAuthAction
  }

  override def beforeEach() = {
    when(mockIncomeService.getIncomeData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))
  }

  "Opening link if user has no income tax or cg tax liability" should {

    "show no ats page for total-income-tax" in {

      val mockTotalIncomeTaxService = mock[TotalIncomeTaxService]

      def sut = new TotalIncomeTaxController(mockTotalIncomeTaxService) {

        override val auditService = mockAuditService
        override val authAction: AuthAction = FakeAuthAction

        when(mockTotalIncomeTaxService.getIncomeData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))
      }
      val result = Future.successful(sut.show(request))

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
    }
  }

  "show have the correct title for the no ATS page" in {

    val result = Future.successful(incomeController.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for income-before-tax" in {

    val result = Future.successful(incomeController.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for tax-free-amount" in {

    val allowanceService = mock[AllowanceService]

    def sut = new AllowancesController(allowanceService) {

      override val auditService = mockAuditService
      override val authAction: AuthAction = FakeAuthAction

      when(allowanceService.getAllowances(Matchers.eq(taxYear))(Matchers.eq(request), Matchers.any())).thenReturn(Future.successful(model))
    }

    val result = Future.successful(sut.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for capital-gains-tax" in {

    val mockCapitalGainsService = mock[CapitalGainsService]

    def sut = new CapitalGainsTaxController(mockCapitalGainsService) {

      override val auditService = mockAuditService
      override val authAction: AuthAction = FakeAuthAction

      when(mockCapitalGainsService.getCapitalGains(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))
    }

    val result = Future.successful(sut.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for government spend" in {

    val mockGovernmentSpendService = mock[GovernmentSpendService]

    def sut = new GovernmentSpendController(mockGovernmentSpendService) {

      override val auditService = mockAuditService
      override val authAction: AuthAction = FakeAuthAction

      when(mockGovernmentSpendService.getGovernmentSpendData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))
    }

    val result = Future.successful(sut.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for summary page" in {

    def sut = new SummaryController(mockSummaryService) {
      override val auditService = mockAuditService
      override val authAction: AuthAction = FakeAuthAction

    }
    when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))

    val result = Future.successful(sut.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for nics summary page" in {

    def sut = new NicsController(mockSummaryService) {
      override val auditService = mockAuditService
      override val authAction: AuthAction = FakeAuthAction

      when(mockSummaryService.getSummaryData(Matchers.eq(taxYear))(Matchers.any(), Matchers.eq(request))).thenReturn(Future.successful(model))
    }

    val result = Future.successful(sut.show(request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }
}
