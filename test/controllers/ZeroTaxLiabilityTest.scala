/*
 * Copyright 2017 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, _}
import services._
import uk.gov.hmrc.play.frontend.auth.{AuthContext => User}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.AuthorityUtils
import view_models.NoATSViewModel
import scala.concurrent.Future
import utils.TestConstants._

class ZeroTaxLiabilityTest extends UnitSpec with FakeTaxsPlayApplication with MockitoSugar {

  val request = FakeRequest()
  val user = User(AuthorityUtils.saAuthority(testOid, testUtr))
  val dataPath = "/no_ats_json_test.json"
  val model = new NoATSViewModel

  "Opening link if user has no income tax or cg tax liability" should {

    "show no ats page for total-income-tax" in new TotalIncomeTaxController {

      override lazy val totalIncomeTaxService = mock[TotalIncomeTaxService]
      override lazy val auditService = mock[AuditService]

      when(totalIncomeTaxService.getIncomeData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

      val result = Future.successful(show(user, request))

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
    }
  }

  "show have the correct title for the no ATS page" in new IncomeController {

    override lazy val incomeService = mock[IncomeService]
    override lazy val auditService = mock[AuditService]

    when(incomeService.getIncomeData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for income-before-tax" in new IncomeController {

    override lazy val incomeService = mock[IncomeService]
    override lazy val auditService = mock[AuditService]

    when(incomeService.getIncomeData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for tax-free-amount" in new AllowancesController {

    override lazy val allowanceService = mock[AllowanceService]
    override lazy val auditService = mock[AuditService]

    when(allowanceService.getAllowances(any[User], any[Request[AnyRef]], any[HeaderCarrier])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for capital-gains-tax" in new CapitalGainsTaxController {

    override lazy val capitalGainsService = mock[CapitalGainsService]
    override lazy val auditService = mock[AuditService]

    when(capitalGainsService.getCapitalGains(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for government spend" in new GovernmentSpendController {

    override lazy val governmentSpendService = mock[GovernmentSpendService]
    override lazy val auditService = mock[AuditService]

    when(governmentSpendService.getGovernmentSpendData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for summary page" in new SummaryController {

    override lazy val summaryService = mock[SummaryService]
    override lazy val auditService = mock[AuditService]

    when(summaryService.getSummaryData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }

  "show no ats page for nics summary page" in new NicsController {

    override lazy val summaryService = mock[SummaryService]
    override lazy val auditService = mock[AuditService]

    when(summaryService.getSummaryData(any[User], any[HeaderCarrier], any[Request[AnyRef]])).thenReturn(model)

    val result = Future.successful(show(user, request))

    status(result) shouldBe 303
    redirectLocation(result) shouldBe Some("/annual-tax-summary/no-ats")
  }
}
