// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.config

import cats.implicits.*
import coulomb.Quantity
import coulomb.ops.algebra.spire.all.given
import coulomb.policy.spire.standard.given
import crystal.react.View
import eu.timepit.refined.auto.*
import eu.timepit.refined.cats.*
import eu.timepit.refined.types.numeric.PosBigDecimal
import explore.components.HelpIcon
import explore.components.ui.ExploreStyles
import explore.model.AvailableFilter
import explore.model.ImagingConfigurationOptions
import explore.model.enums.ImagingCapabilities
import explore.model.formats.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.feature.ReactFragment
import japgolly.scalajs.react.vdom.html_<^.*
import lucuma.core.enums.FilterType
import lucuma.core.math.units.*
import lucuma.core.util.Display
import lucuma.core.validation.*
import lucuma.refined.*
import lucuma.ui.forms.EnumViewOptionalSelect
import lucuma.ui.forms.FormInputEV
import lucuma.ui.input.ChangeAuditor
import lucuma.ui.primereact.FormEnumDropdownOptionalView
import lucuma.ui.primereact.FormInputTextView
import lucuma.ui.primereact.LucumaStyles
import lucuma.ui.primereact.given
import lucuma.ui.syntax.all.given
import react.common.Css
import react.common.ReactFnProps
import react.primereact.MultiSelect
import react.primereact.PrimeStyles
import react.primereact.SelectItem
import react.primereact.SelectItemGroup
import react.primereact.SelectItemGroups
import spire.math.Rational

import scala.collection.immutable.SortedSet
import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

case class ImagingConfigurationPanel(
  options: View[ImagingConfigurationOptions]
) extends ReactFnProps(ImagingConfigurationPanel.component)

object ImagingConfigurationPanel {
  private type Props         = ImagingConfigurationPanel
  private type SectionHeader = String

  private given Display[ImagingCapabilities] = Display.by(_.label, _.label)

  private val byFilterType = ImagingConfigurationOptions.availableOptions.groupBy(_.filterType)
  private val broadBand    =
    byFilterType.getOrElse(FilterType.BroadBand, Nil).sortBy(_.centralWavelength)
  private val narrowBand   =
    byFilterType.getOrElse(FilterType.NarrowBand, Nil).sortBy(_.centralWavelength)
  private val combination  =
    byFilterType.getOrElse(FilterType.Combination, Nil).sortBy(_.centralWavelength)

  private def formatCentral(r: Quantity[PosBigDecimal, Micrometer]): String =
    if (r.value > 1000)
      f"${r.value.value.toDouble}%.3f μm"
    else
      s"${r.value.toInt} nm"

  private def formatRange(r: Quantity[Int, Nanometer]): String =
    s"${r.value.toInt} nm"

  extension (filter: AvailableFilter)
    def toSelectItem: SelectItem[AvailableFilter] =
      SelectItem(value = filter, label = filter.shortName)

  private val filterGroups: SelectItemGroups[AvailableFilter] = SelectItemGroups(groups =
    List(
      SelectItemGroup(label = "Broad Band", options = broadBand.map(_.toSelectItem)),
      SelectItemGroup(label = "Narrow Band", options = narrowBand.map(_.toSelectItem)),
      SelectItemGroup(label = "Combination", options = combination.map(_.toSelectItem))
    )
  )

  protected val component =
    ScalaFnComponent[Props] { p =>
      val filters       = p.options.zoom(ImagingConfigurationOptions.filters)
      val fov           = p.options.zoom(ImagingConfigurationOptions.fov)
      val signalToNoise = p.options.zoom(ImagingConfigurationOptions.signalToNoise)
      val capabilities  = p.options.zoom(ImagingConfigurationOptions.capabilities)

      ReactFragment(
        <.label("Filter", HelpIcon("configuration/filter.md".refined), LucumaStyles.FormFieldLabel),
        MultiSelect(
          id = "filters",
          value = filters.get.toList,
          options = filterGroups,
          clazz = LucumaStyles.FormField,
          panelClass = ExploreStyles.ConfigurationFilter,
          filter = true,
          showSelectAll = false,
          display = MultiSelect.Display.Chip,
          onChange = fs => filters.set(SortedSet.from(fs)),
          itemTemplate = si =>
            <.div(
              <.span(si.value.shortName),
              <.span(formatCentral(si.value.centralWavelength.toMicrometers)),
              <.span(si.value.range.map(formatRange))
            )
        ),
        FormInputTextView(
          id = "configuration-fov".refined,
          value = fov,
          label = ReactFragment("Field of View", HelpIcon("configuration/fov.md".refined)),
          units = "arcsec",
          validFormat = InputValidWedge.fromFormat(formatArcsec).optional,
          changeAuditor = ChangeAuditor.fromFormat(formatArcsec).optional
        ),
        FormInputTextView(
          id = "signal-to-noise".refined,
          value = signalToNoise,
          label = ReactFragment("S / N", HelpIcon("configuration/signal_to_noise.md".refined)),
          validFormat = InputValidSplitEpi.posBigDecimal.optional,
          changeAuditor = ChangeAuditor.posBigDecimal().optional
        ),
        FormEnumDropdownOptionalView(
          id = "imaging-capabilities".refined,
          label = ReactFragment(
            "Capabilities",
            HelpIcon("configuration/capabilities.md".refined)
          ),
          placeholder = "Extra capablities",
          value = capabilities
        )
      )
    }
}
