// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model

import cats.Eq
import cats.data.NonEmptyList
import cats.derived.*
import cats.syntax.all.*
import crystal.react.View
import eu.timepit.refined.cats.*
import explore.model.enums.AgsState
import lucuma.ags.*
import lucuma.core.math.Offset
import lucuma.core.math.Wavelength
import lucuma.core.model.ConstraintSet
import lucuma.core.model.PosAngleConstraint
import lucuma.schemas.model.BasicConfiguration
import monocle.Focus
import org.typelevel.cats.time.instantInstances

import java.time.Instant

case class ObsConfiguration(
  configuration:      Option[BasicConfiguration],
  posAngleProperties: Option[PAProperties],
  constraints:        Option[ConstraintSet],
  wavelength:         Option[Wavelength],
  scienceOffsets:     Option[NonEmptyList[Offset]],
  acquisitionOffsets: Option[NonEmptyList[Offset]]
) derives Eq:
  def posAngleConstraint: Option[PosAngleConstraint] = posAngleProperties.map(_.constraint.get)

  def posAngleConstraintView: Option[View[PosAngleConstraint]] =
    posAngleProperties.map(_.constraint)

  def agsState: Option[View[AgsState]] =
    posAngleProperties.map(_.agsState)

  def selectedGS: Option[View[Option[AgsAnalysis]]] =
    posAngleProperties.map(_.selectedGS)
