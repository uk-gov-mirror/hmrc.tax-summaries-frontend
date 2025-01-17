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

import connectors.{DataCacheConnector, MiddleConnector}
import controllers.auth.AuthenticatedRequest
import models._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{SaUtr, Uar}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtil._
import utils.TestConstants._
import utils.{AccountUtils, AuthorityUtils, GenericViewModel}
import view_models.{ATSUnavailableViewModel, NoATSViewModel}

import scala.concurrent.{ExecutionContext, Future}

class AtsServiceSpec
    extends UnitSpec with GuiceOneAppPerSuite with ScalaFutures with MockitoSugar with BeforeAndAfterEach {

  val data = {
    val json = loadAndParseJsonWithDummyData("/summary_json_test.json")
    Json.fromJson[AtsData](json).get
  }

  val mockMiddleConnector: MiddleConnector = mock[MiddleConnector]
  val mockDataCacheConnector: DataCacheConnector = mock[DataCacheConnector]
  val mockAuditService: AuditService = mock[AuditService]
  val mockAuthUtils: AuthorityUtils = mock[AuthorityUtils]
  val mockAccountUtils: AccountUtils = mock[AccountUtils]

  override def beforeEach() = {
    reset(mockMiddleConnector)
    reset(mockDataCacheConnector)
    reset(mockAuditService)
    reset(mockAuthUtils)
    reset(mockAccountUtils)

  }

  implicit val hc = new HeaderCarrier
  implicit lazy val ec = app.injector.instanceOf[ExecutionContext]
  implicit val request =
    AuthenticatedRequest(
      "userId",
      None,
      Some(SaUtr(testUtr)),
      None,
      None,
      None,
      None,
      true,
      fakeCredentials,
      FakeRequest())

  val agentToken = AgentToken(
    agentUar = testUar,
    clientUtr = testUtr,
    timestamp = 0
  )

  def sut = new AtsService(mockMiddleConnector, mockDataCacheConnector, mockAuditService, mockAuthUtils) {
    override val accountUtils: AccountUtils = mockAccountUtils
  }

  case class FakeViewModel(str: String) extends GenericViewModel

  def converter(atsData: AtsData): FakeViewModel = FakeViewModel(atsData.toString)

  "AtsService" when {

    "createModel is called" must {

      "return an instance of the desired view model" when {

        "connector returns a success response with valid payload" which {

          "a user who is not an agent" that {

            "has AtsData present in the cache" in {

              when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(data)

              when(mockAccountUtils.isAgent(any())) thenReturn false

              when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsSuccessResponseWithPayload[
                AtsData](data)

              when(mockDataCacheConnector.storeAtsForSession(eqTo(data))(any(), any())) thenReturn Some(data)

              when(mockAuditService.sendEvent(any(), any(), any())(any(), any())) thenReturn Future.successful(Success)

              sut.createModel(fakeTaxYear, converter).futureValue shouldBe FakeViewModel(data.toString)

              verify(mockAuditService).sendEvent(any(), any(), any())(any(), any())
            }

            "has no data in the cache" in {

              when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn None

              when(mockAccountUtils.isAgent(any())) thenReturn false

              when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsSuccessResponseWithPayload[
                AtsData](data)

              when(mockDataCacheConnector.storeAtsForSession(eqTo(data))(any(), any())) thenReturn Some(data)

              when(mockAuditService.sendEvent(any(), any(), any())(any(), any())) thenReturn Future.successful(Success)

              sut.createModel(fakeTaxYear, converter).futureValue shouldBe FakeViewModel(data.toString)

              verify(mockAuditService).sendEvent(any(), any(), any())(any(), any())
            }
          }

          "a user who is an agent" that {

            "has AtsData present in the cache" in {

              when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(data)

              when(mockAccountUtils.isAgent(any())) thenReturn true

              when(mockAuthUtils.checkUtr(eqTo(Some(testUtr)), any())(any())) thenReturn true

              when(mockDataCacheConnector.getAgentToken(any(), any())) thenReturn Some(agentToken)

              sut.createModel(fakeTaxYear, converter).futureValue shouldBe FakeViewModel(data.toString)
            }

            "has no data in the cache" in {

              when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn None

              when(mockAccountUtils.isAgent(any())) thenReturn true

              when(mockAuthUtils.getRequestedUtr(eqTo(Uar(testUar)), any())) thenReturn SaUtr(testNonMatchingUtr)

              when(mockAccountUtils.getAccount(any())) thenReturn Uar(testUar)

              when(mockDataCacheConnector.getAgentToken(any(), any())) thenReturn Some(agentToken)

              when(mockAuditService.sendEvent(any(), any(), any())(any(), any())) thenReturn Future.successful(Success)

              when(
                mockMiddleConnector.connectToAtsOnBehalfOf(
                  eqTo(Uar(testUar)),
                  eqTo(SaUtr(testNonMatchingUtr)),
                  eqTo(fakeTaxYear))(any())) thenReturn AtsSuccessResponseWithPayload[AtsData](data)

              when(mockDataCacheConnector.storeAtsForSession(eqTo(data))(any(), any())) thenReturn Some(data)

              implicit val request =
                AuthenticatedRequest(
                  "userId",
                  Some(Uar(testUar)),
                  Some(SaUtr(testUtr)),
                  None,
                  None,
                  None,
                  None,
                  true,
                  fakeCredentials,
                  FakeRequest())

              sut.createModel(fakeTaxYear, converter).futureValue shouldBe FakeViewModel(data.toString)

              verify(mockAuditService).sendEvent(any(), any(), any())(any(), any())
            }
          }
        }
      }

      "return a NoATSViewModel" when {

        "the connector returns a NoATSViewModel" in {

          when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(data)

          when(mockAccountUtils.isAgent(any())) thenReturn false

          when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsNotFoundResponse(
            "Not found")

          sut.createModel(fakeTaxYear, converter).futureValue shouldBe a[NoATSViewModel]

          verify(mockAuditService, never()).sendEvent(any(), any(), any())(any(), any())
        }

        "there is a NoAtsError in the AtsData" in {

          val dataWithError = data.copy(errors = Some(IncomingAtsError("NoAtsError")))

          when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(dataWithError)

          when(mockAccountUtils.isAgent(any())) thenReturn false

          when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsSuccessResponseWithPayload(
            dataWithError)

          sut.createModel(fakeTaxYear, converter).futureValue shouldBe a[NoATSViewModel]

          verify(mockAuditService, never()).sendEvent(any(), any(), any())(any(), any())
        }
      }

      "return an AtsUnavailableViewModel" when {

        "the connector returns an AtsErrorResponse" in {

          when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(data)

          when(mockAccountUtils.isAgent(any())) thenReturn false

          when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsErrorResponse(
            "Something went wrong")

          sut.createModel(fakeTaxYear, converter).futureValue shouldBe a[ATSUnavailableViewModel]

          verify(mockAuditService, never()).sendEvent(any(), any(), any())(any(), any())
        }

        "there is any other error in the AtsData" in {

          val dataWithError = data.copy(errors = Some(IncomingAtsError("Random error")))

          when(mockDataCacheConnector.fetchAndGetAtsForSession(eqTo(fakeTaxYear))(any())) thenReturn Some(dataWithError)

          when(mockAccountUtils.isAgent(any())) thenReturn false

          when(mockMiddleConnector.connectToAts(eqTo(SaUtr(testUtr)), eqTo(fakeTaxYear))(any())) thenReturn AtsSuccessResponseWithPayload(
            dataWithError)

          sut.createModel(fakeTaxYear, converter).futureValue shouldBe a[ATSUnavailableViewModel]

          verify(mockAuditService, never()).sendEvent(any(), any(), any())(any(), any())
        }
      }
    }
  }
}
