// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model

import cats.Eq
import cats.syntax.all._
import io.circe.Decoder
import io.circe.generic.semiauto._
import lucuma.core.enum.GmosNorthFilter
import lucuma.core.enum.GmosNorthFpu
import lucuma.core.enum.GmosNorthGrating
import lucuma.core.enum.GmosSouthFilter
import lucuma.core.enum.GmosSouthFpu
import lucuma.core.enum.GmosSouthGrating

sealed trait ScienceModeBasic extends Product with Serializable

object ScienceModeBasic {
  implicit val scienceModeBasicEq: Eq[ScienceModeBasic] =
    Eq.instance {
      case (a: GmosNorthLongSlit, b: GmosNorthLongSlit) => a === b
      case (a: GmosSouthLongSlit, b: GmosSouthLongSlit) => a === b
      case _                                            => false
    }

  final case class GmosNorthLongSlit(
    grating: GmosNorthGrating,
    filter:  Option[GmosNorthFilter],
    fpu:     GmosNorthFpu
  ) extends ScienceModeBasic

  object GmosNorthLongSlit {
    implicit val gmosNLongSlitEq: Eq[GmosNorthLongSlit] =
      Eq.by(x => (x.grating, x.filter, x.fpu))

    implicit val gmosNLongSlitDecoder: Decoder[GmosNorthLongSlit] = deriveDecoder
  }

  final case class GmosSouthLongSlit(
    grating: GmosSouthGrating,
    filter:  Option[GmosSouthFilter],
    fpu:     GmosSouthFpu
  ) extends ScienceModeBasic

  object GmosSouthLongSlit {
    implicit val gmosSLongSlitEq: Eq[GmosSouthLongSlit] =
      Eq.by(x => (x.grating, x.filter, x.fpu))

    implicit val gmosSLongSlitDecoder: Decoder[GmosSouthLongSlit] = deriveDecoder
  }
}
