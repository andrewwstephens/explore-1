// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.optics

import eu.timepit.refined.cats.*
import eu.timepit.refined.scalacheck.all.*
import explore.optics.all.*
import monocle.law.discipline.IsoTests
import munit.DisciplineSuite

class OptionNonEmptyStringIsoSuite extends DisciplineSuite {

  // This is in it's own test suite because I was unable to resolve
  // an implicit expansion compiler error when it was in ModelOpticsSuite
  checkAll("optionNonEmptyStringIso", IsoTests(optionNonEmptyStringIso))
}
