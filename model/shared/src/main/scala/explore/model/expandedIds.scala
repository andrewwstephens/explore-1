// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model

import cats.Order._
import cats._
import lucuma.core.model.Asterism
import lucuma.core.model.Target
import monocle.Focus

import scala.collection.immutable.SortedSet

case class ExpandedIds(
  targetIds:   SortedSet[Target.Id] = SortedSet.empty,
  asterismIds: SortedSet[Asterism.Id] = SortedSet.empty
  // constraintSetIds: SortedSet[ConstraintSet.Id] = SortedSet.empty
)

object ExpandedIds {
  val targetIds   = Focus[ExpandedIds](_.targetIds)
  val asterismIds = Focus[ExpandedIds](_.asterismIds)
  // val constraintSetIds = Focus[ExpandedIds](_.constraintSetIds)

  implicit val eqExpandedIds: Eq[ExpandedIds] = Eq.fromUniversalEquals
}
