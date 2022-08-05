// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model.boopickle

import boopickle.DefaultBasic._
import cats.data.NonEmptyMap
import cats.implicits._
import coulomb.Quantity
import eu.timepit.refined.types.numeric.NonNegBigDecimal
import eu.timepit.refined.types.numeric.PosBigDecimal
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import explore.model.itc.ItcQueryProblems
import explore.model.itc.ItcRequestParams
import explore.model.itc.ItcResult
import explore.model.itc.ItcTarget
import explore.modes.InstrumentRow
import explore.modes.ModeAO
import explore.modes.ModeSlitSize
import explore.modes.ModeWavelength
import explore.modes.SpectroscopyModeRow
import explore.modes.SpectroscopyModesMatrix
import explore.modes._
import lucuma.core.enums.FocalPlane
import lucuma.core.enums.SpectroscopyCapabilities
import lucuma.core.math.BrightnessUnits
import lucuma.core.math.BrightnessUnits.Brightness
import lucuma.core.math.BrightnessUnits.FluxDensityContinuum
import lucuma.core.math.BrightnessUnits.LineFlux
import lucuma.core.math.RadialVelocity
import lucuma.core.math.Wavelength
import lucuma.core.math.dimensional._
import lucuma.core.math.units._
import lucuma.core.model.EmissionLine
import lucuma.core.model.SourceProfile
import lucuma.core.model.SpectralDefinition
import lucuma.core.model.UnnormalizedSED

import scala.collection.immutable.SortedMap

// Boopicklers for itc related types
trait ItcPicklers extends CommonPicklers {

  implicit val gmosNPickler: Pickler[GmosNorthSpectroscopyRow] =
    transformPickler(Function.tupled(GmosNorthSpectroscopyRow.apply _))(x =>
      (x.grating, x.fpu, x.filter)
    )

  implicit val gmosSPickler: Pickler[GmosSouthSpectroscopyRow] =
    transformPickler(Function.tupled(GmosSouthSpectroscopyRow.apply _))(x =>
      (x.grating, x.fpu, x.filter)
    )

  implicit val f2Pickler: Pickler[Flamingos2SpectroscopyRow] =
    transformPickler(Function.tupled(Flamingos2SpectroscopyRow.apply _))(x => (x.grating, x.filter))

  implicit val gpiPickler: Pickler[GpiSpectroscopyRow] =
    transformPickler(Function.tupled(GpiSpectroscopyRow.apply _))(x => (x.grating, x.filter))

  implicit val gnirsPickler: Pickler[GnirsSpectroscopyRow] =
    transformPickler(Function.tupled(GnirsSpectroscopyRow.apply _))(x => (x.grating, x.filter))

  implicit val genericRowPickler: Pickler[GenericSpectroscopyRow] =
    transformPickler(Function.tupled(GenericSpectroscopyRow.apply _))(x =>
      (x.i, x.grating, x.filter)
    )

  implicit val instRowPickler: Pickler[InstrumentRow] =
    compositePickler[InstrumentRow]
      .addConcreteType[GmosNorthSpectroscopyRow]
      .addConcreteType[GmosSouthSpectroscopyRow]
      .addConcreteType[Flamingos2SpectroscopyRow]
      .addConcreteType[GpiSpectroscopyRow]
      .addConcreteType[GnirsSpectroscopyRow]
      .addConcreteType[GenericSpectroscopyRow]

  implicit val mwPickler: Pickler[ModeWavelength] =
    transformPickler(ModeWavelength.apply)(_.w)

  implicit val msPickler: Pickler[ModeSlitSize] =
    transformPickler(ModeSlitSize.apply)(_.size)

  implicit val rowPickler: Pickler[SpectroscopyModeRow] =
    transformPickler(
      (x: Tuple13[
        Int,
        InstrumentRow,
        NonEmptyString,
        FocalPlane,
        Option[SpectroscopyCapabilities],
        ModeAO,
        ModeWavelength,
        ModeWavelength,
        ModeWavelength,
        Quantity[NonNegBigDecimal, Micrometer],
        PosInt,
        ModeSlitSize,
        ModeSlitSize
      ]) =>
        x match {
          case (
                id,
                instrument,
                config,
                focalPlane,
                capabilities,
                ao,
                minWavelength,
                maxWavelength,
                optimalWavelength,
                wavelengthCoverage,
                resolution,
                slitLength,
                slitWidth
              ) =>
            SpectroscopyModeRow(id,
                                instrument,
                                config,
                                focalPlane,
                                capabilities,
                                ao,
                                minWavelength,
                                maxWavelength,
                                optimalWavelength,
                                wavelengthCoverage,
                                resolution,
                                slitLength,
                                slitWidth
            )
        }
    )(x =>
      (x.id,
       x.instrument,
       x.config,
       x.focalPlane,
       x.capabilities,
       x.ao,
       x.minWavelength,
       x.maxWavelength,
       x.optimalWavelength,
       x.wavelengthCoverage,
       x.resolution,
       x.slitLength,
       x.slitWidth
      )
    )

  given Pickler[SpectroscopyModesMatrix] =
    transformPickler(SpectroscopyModesMatrix.apply)(_.matrix)

  given Pickler[UnnormalizedSED.StellarLibrary] =
    transformPickler(UnnormalizedSED.StellarLibrary.apply)(_.librarySpectrum)

  given Pickler[UnnormalizedSED.CoolStarModel] =
    transformPickler(UnnormalizedSED.CoolStarModel.apply)(_.temperature)

  given Pickler[UnnormalizedSED.Galaxy] =
    transformPickler(UnnormalizedSED.Galaxy.apply)(_.galaxySpectrum)

  given Pickler[UnnormalizedSED.Planet] =
    transformPickler(UnnormalizedSED.Planet.apply)(_.planetSpectrum)

  given Pickler[UnnormalizedSED.Quasar] =
    transformPickler(UnnormalizedSED.Quasar.apply)(_.quasarSpectrum)

  given Pickler[UnnormalizedSED.HIIRegion] =
    transformPickler(UnnormalizedSED.HIIRegion.apply)(_.hiiRegionSpectrum)

  given Pickler[UnnormalizedSED.PlanetaryNebula] =
    transformPickler(UnnormalizedSED.PlanetaryNebula.apply)(_.planetaryNebulaSpectrum)

  given Pickler[UnnormalizedSED.PowerLaw] =
    transformPickler(UnnormalizedSED.PowerLaw.apply)(_.index)

  given Pickler[UnnormalizedSED.BlackBody] =
    transformPickler(UnnormalizedSED.BlackBody.apply)(_.temperature)

  given Pickler[UnnormalizedSED.UserDefined] =
    transformPickler(UnnormalizedSED.UserDefined.apply)(_.fluxDensities)

  given Pickler[UnnormalizedSED] =
    compositePickler[UnnormalizedSED]
      .addConcreteType[UnnormalizedSED.StellarLibrary]
      .addConcreteType[UnnormalizedSED.CoolStarModel]
      .addConcreteType[UnnormalizedSED.Galaxy]
      .addConcreteType[UnnormalizedSED.Planet]
      .addConcreteType[UnnormalizedSED.Quasar]
      .addConcreteType[UnnormalizedSED.HIIRegion]
      .addConcreteType[UnnormalizedSED.PlanetaryNebula]
      .addConcreteType[UnnormalizedSED.PowerLaw]
      .addConcreteType[UnnormalizedSED.BlackBody]
      .addConcreteType[UnnormalizedSED.UserDefined]

  given taggedMeasurePickler[N: Pickler, T](using Pickler[Units Of T]): Pickler[Measure[N] Of T] =
    transformPickler { (x: (Units Of T, N, Option[N])) =>
      val base = x._1.withValueTagged(x._2)
      x._3.map(base.withError).getOrElse(base)
    }(x => (Measure.unitsTagged.get(x), x.value, x.error))

  given bandNormalizedPickler[A](using
    Pickler[Units Of Brightness[A]]
  ): Pickler[SpectralDefinition.BandNormalized[A]] =
    transformPickler(Function.tupled(SpectralDefinition.BandNormalized.apply[A] _))(x =>
      (x.sed, x.brightnesses)
    )

  given emissionLinesPickler[A](using Pickler[Units Of LineFlux[A]]): Pickler[EmissionLine[A]] =
    transformPickler(Function.tupled(EmissionLine.apply[A] _))(x => (x.lineWidth, x.lineFlux))

  given spectralEmissionLinesPickler[A](using
    Pickler[Units Of LineFlux[A]],
    Pickler[Units Of FluxDensityContinuum[A]]
  ): Pickler[SpectralDefinition.EmissionLines[A]] =
    transformPickler(
      (x: (List[(Wavelength, EmissionLine[A])],
           Measure[PosBigDecimal] Of FluxDensityContinuum[A]
      )) => SpectralDefinition.EmissionLines(SortedMap.from(x._1), x._2)
    )(x => (x.lines.toList, x.fluxDensityContinuum))

  given spectralDefinitionPickler[A](using
    Pickler[Units Of Brightness[A]],
    Pickler[Units Of LineFlux[A]],
    Pickler[Units Of FluxDensityContinuum[A]]
  ): Pickler[SpectralDefinition[A]] =
    compositePickler[SpectralDefinition[A]]
      .addConcreteType[SpectralDefinition.BandNormalized[A]]
      .addConcreteType[SpectralDefinition.EmissionLines[A]]

  given Pickler[SourceProfile.Point] =
    transformPickler(SourceProfile.Point.apply)(_.spectralDefinition)

  given Pickler[SourceProfile.Uniform] =
    transformPickler(SourceProfile.Uniform.apply)(_.spectralDefinition)

  given Pickler[SourceProfile.Gaussian] =
    transformPickler(Function.tupled(SourceProfile.Gaussian.apply _))(x =>
      (x.fwhm, x.spectralDefinition)
    )

  given Pickler[SourceProfile] =
    compositePickler[SourceProfile]
      .addConcreteType[SourceProfile.Point]
      .addConcreteType[SourceProfile.Uniform]
      .addConcreteType[SourceProfile.Gaussian]

  given Pickler[ItcTarget] =
    transformPickler(Function.tupled(ItcTarget.apply _))(x => (x.rv, x.profile))

  given Pickler[ItcResult.SourceTooBright.type] = generatePickler
  given Pickler[ItcResult.Pending.type]         = generatePickler
  given Pickler[ItcResult.Result]               =
    transformPickler(Function.tupled(ItcResult.Result.apply _))(x => (x.duration, x.exposures))

  given Pickler[ItcResult] =
    compositePickler[ItcResult]
      .addConcreteType[ItcResult.SourceTooBright.type]
      .addConcreteType[ItcResult.Pending.type]
      .addConcreteType[ItcResult.Result]

  given Pickler[ItcQueryProblems.UnsupportedMode.type]      = generatePickler
  given Pickler[ItcQueryProblems.MissingWavelength.type]    = generatePickler
  given Pickler[ItcQueryProblems.MissingSignalToNoise.type] = generatePickler
  given Pickler[ItcQueryProblems.MissingTargetInfo.type]    = generatePickler
  given Pickler[ItcQueryProblems.GenericError]              =
    transformPickler(ItcQueryProblems.GenericError.apply)(_.msg)

  given Pickler[ItcQueryProblems] =
    compositePickler[ItcQueryProblems]
      .addConcreteType[ItcQueryProblems.UnsupportedMode.type]
      .addConcreteType[ItcQueryProblems.MissingWavelength.type]
      .addConcreteType[ItcQueryProblems.MissingSignalToNoise.type]
      .addConcreteType[ItcQueryProblems.MissingTargetInfo.type]
      .addConcreteType[ItcQueryProblems.GenericError]

  given Pickler[ItcRequestParams] = generatePickler

}

object ItcPicklers extends ItcPicklers
