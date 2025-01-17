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

import controllers.auth.AuthenticatedRequest
import controllers.paye.AppConfigBaseSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n
import play.api.i18n.{MessagesApi, MessagesImpl, _}
import play.api.mvc.{DefaultMessagesActionBuilderImpl, MessagesActionBuilder, _}
import play.api.test.FakeRequest
import play.api.test.Helpers.{stubBodyParser, stubControllerComponents, stubMessagesApi}
import services.PayeAtsService
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.partials.FormPartialRetriever
import utils.MockTemplateRenderer
import utils.TestConstants.testUtr
import views.html.errors.{GenericErrorView, ServiceUnavailableView, _}
import views.html.{IncomeBeforeTaxView, NicsView, SummaryView, _}

import scala.concurrent.ExecutionContext

trait ControllerBaseSpec extends AppConfigBaseSpec with MockitoSugar {

  private val messagesActionBuilder: MessagesActionBuilder =
    new DefaultMessagesActionBuilderImpl(stubBodyParser[AnyContent](), stubMessagesApi())
  private val cc: ControllerComponents = stubControllerComponents()

  lazy val mcc: MessagesControllerComponents = DefaultMessagesControllerComponents(
    messagesActionBuilder,
    DefaultActionBuilder(stubBodyParser[AnyContent]()),
    cc.parsers,
    fakeApplication.injector.instanceOf[MessagesApi],
    cc.langs,
    cc.fileMimeTypes,
    ExecutionContext.global
  )

  implicit lazy val lang: Lang = Lang(java.util.Locale.getDefault)
  implicit lazy val testMessages: MessagesImpl = MessagesImpl(i18n.Lang("en"), mcc.messagesApi)

  val mockPayeAtsService: PayeAtsService = mock[PayeAtsService]
  implicit lazy val formPartialRetriever = inject[FormPartialRetriever]
  implicit lazy val templateRenderer = MockTemplateRenderer

  implicit val ec: ExecutionContext = mcc.executionContext

  lazy val taxFreeAmountView = inject[TaxFreeAmountView]
  lazy val genericErrorView = inject[GenericErrorView]
  lazy val tokenErrorView = inject[TokenErrorView]
  lazy val taxsMainView = inject[TaxsMainView]
  lazy val capitalGainsView = inject[CapitalGainsView]
  lazy val notAuthorisedView = inject[NotAuthorisedView]
  lazy val howTaxIsSpentView = inject[HowTaxIsSpentView]
  lazy val serviceUnavailableView = inject[ServiceUnavailableView]
  lazy val governmentSpendingView = inject[GovernmentSpendingView]
  lazy val incomeBeforeTaxView = inject[IncomeBeforeTaxView]
  lazy val taxsIndexView = inject[TaxsIndexView]
  lazy val totalIncomeTaxView = inject[TotalIncomeTaxView]
  lazy val summaryView = inject[SummaryView]
  lazy val nicsView = inject[NicsView]

  val fakeCredentials = new Credentials("provider ID", "provider type")

  lazy val request = AuthenticatedRequest(
    "userId",
    None,
    Some(SaUtr(testUtr)),
    None,
    None,
    None,
    None,
    true,
    fakeCredentials,
    FakeRequest("GET", s"?taxYear=$taxYear"))

  lazy val badRequest = AuthenticatedRequest(
    "userId",
    None,
    Some(SaUtr(testUtr)),
    None,
    None,
    None,
    None,
    true,
    fakeCredentials,
    FakeRequest("GET", "?taxYear=20145"))

}
