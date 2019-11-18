/*
 * Copyright 2019 HM Revenue & Customs
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

package services.atsData

import models.{AtsData, DataHolder, UserData}
import view_models.{Amount, Rate}

object AtsTestData {

  val atsAllowancesData = AtsData(
    2019,
    Some("1111111111"),
    None,
    None,
    None,
    Some(
      DataHolder(
        Some(
          Map(
            "personal_tax_free_amount" -> Amount(100, "GBP"),
            "marriage_allowance_transferred_amount" -> Amount(200, "GBP"),
            "other_allowances_amount" -> Amount(300, "GBP"),
            "total_tax_free_amount" -> Amount(400, "GBP")
          )
        ),
        None,
        None
      )
    ),
    None,
    None,
    Some(
      UserData(
        Some(
          Map(
            "title" -> "Mr",
            "forename" -> "John",
            "surname" -> "Smith"
          )
        )
      )
    ),
    None
  )

  val incomeData = AtsData(
    2019,
    Some("1111111111"),
    None,
    None,
    Some(
      DataHolder(
        Some(
          Map(
            "self_employment_income" -> Amount(100, "GBP"),
            "income_from_employment" -> Amount(200, "GBP"),
            "state_pension" -> Amount(300, "GBP"),
            "other_pension_income" -> Amount(400, "GBP"),
            "taxable_state_benefits" -> Amount(500, "GBP"),
            "other_income" -> Amount(600, "GBP"),
            "benefits_from_employment" -> Amount(700, "GBP"),
            "total_income_before_tax" -> Amount(800, "GBP")
          )
        ),
        None,
        None
      )
    ),
    None,
    None,
    None,
    Some(
      UserData(
        Some(
          Map(
            "title" -> "Mr",
            "forename" -> "John",
            "surname" -> "Smith"
          )
        )
      )
    ),
    None
  )

  val totalIncomeTaxData = AtsData(
    2019,
    Some("1111111111"),
    Some(
      DataHolder(
        Some(
          Map(
            "starting_rate_for_savings" -> Amount(100, "GBP"),
            "starting_rate_for_savings_amount" -> Amount(200, "GBP"),
            "basic_rate_income_tax" -> Amount(300, "GBP"),
            "basic_rate_income_tax_amount" -> Amount(400, "GBP"),
            "higher_rate_income_tax" -> Amount(500, "GBP"),
            "higher_rate_income_tax_amount" -> Amount(600, "GBP"),
            "additional_rate_income_tax" -> Amount(700, "GBP"),
            "additional_rate_income_tax_amount" -> Amount(800, "GBP"),
            "ordinary_rate" -> Amount(900, "GBP"),
            "ordinary_rate_amount" -> Amount(1000, "GBP"),
            "upper_rate" -> Amount(1100, "GBP"),
            "upper_rate_amount" -> Amount(1200, "GBP"),
            "additional_rate" -> Amount(1300, "GBP"),
            "additional_rate_amount" -> Amount(1400, "GBP"),
            "other_adjustments_increasing" -> Amount(1500, "GBP"),
            "marriage_allowance_received_amount" -> Amount(1600, "GBP"),
            "other_adjustments_reducing" -> Amount(1700, "GBP"),
            "total_income_tax" -> Amount(1800, "GBP"),
            "scottish_income_tax" -> Amount(1900, "GBP")
          )
        ),
        Some(
          Map(
            "starting_rate_for_savings_rate" -> Rate("10%"),
            "basic_rate_income_tax_rate" -> Rate("20%"),
            "higher_rate_income_tax_rate" -> Rate("30%"),
            "additional_rate_income_tax_rate" -> Rate("40%"),
            "ordinary_rate_tax_rate" -> Rate("50%"),
            "upper_rate_rate" -> Rate("60%"),
            "additional_rate_rate" -> Rate("70%")
          )
        ),
        Some("0002")
      )
    ),
    None,
    None,
    None,
    None,
    None,
    Some(
      UserData(
        Some(
          Map(
            "title" -> "Mr",
            "forename" -> "John",
            "surname" -> "Smith"
          )
        )
      )
    ),
    None
  )
}
