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

package utils

import config.ApplicationConfig
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{TaxRegime, GovernmentGateway}

object TAXSGovernmentGateway extends GovernmentGateway {
  override def continueURL: String = ApplicationConfig.loginCallback
  override def loginURL: String = ApplicationConfig.loginUrl
}

object TaxSummariesRegime extends TaxRegime {
  override def isAuthorised(accounts: Accounts) = accounts.sa.isDefined || accounts.taxsAgent.isDefined
  override val unauthorisedLandingPage = Some(controllers.routes.ErrorController.notAuthorised().url)
  override val authenticationType = TAXSGovernmentGateway
}
