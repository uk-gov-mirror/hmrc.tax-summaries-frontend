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

package view_models

import utils.GenericViewModel

case class TotalIncomeTax(year: Int,
                      utr: String,
                      startingRateForSavings: Amount,
                      startingRateForSavingsAmount: Amount,
                      basicRateIncomeTax: Amount,
                      basicRateIncomeTaxAmount: Amount,
                      higherRateIncomeTax: Amount,
                      higherRateIncomeTaxAmount: Amount,
                      additionalRateIncomeTax: Amount,
                      additionalRateIncomeTaxAmount: Amount,
                      ordinaryRate: Amount,
                      ordinaryRateAmount: Amount,
                      upperRate: Amount,
                      upperRateAmount: Amount,
                      additionalRate: Amount,
                      additionalRateAmount: Amount,
                      otherAdjustmentsIncreasing: Amount,
                      otherAdjustmentsReducing: Amount,
                      totalIncomeTax: Amount,
                      startingRateForSavingsRateRate: Rate,
                      basicRateIncomeTaxRateRate: Rate,
                      higherRateIncomeTaxRateRate: Rate,
                      additionalRateIncomeTaxRateRate: Rate,
                      ordinaryRateTaxRateRate: Rate,
                      upperRateRateRate: Rate,
                      additionalRateRateRate: Rate,
                      title: String,
                      forename: String,
                      surname: String)  extends GenericViewModel  {
  def taxYear = year.toString

  def startingRateForSavingsRate = startingRateForSavingsRateRate.percent
  def basicRateIncomeTaxRate = basicRateIncomeTaxRateRate.percent
  def higherRateIncomeTaxRate = higherRateIncomeTaxRateRate.percent
  def additionalRateIncomeTaxRate = additionalRateIncomeTaxRateRate.percent
  def ordinaryRateTaxRate = ordinaryRateTaxRateRate.percent
  def upperRateRate = upperRateRateRate.percent
  def additionalRateRate = additionalRateRateRate.percent
  def taxYearFrom = (year-1).toString
}
