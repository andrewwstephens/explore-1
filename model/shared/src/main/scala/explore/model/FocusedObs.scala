// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model

import cats.kernel.Eq
import lucuma.core.model.Observation

case class FocusedObs(obsId: Observation.Id)

object FocusedObs {
  implicit val eqFocusedObs: Eq[FocusedObs] = Eq.by(_.obsId)
}
