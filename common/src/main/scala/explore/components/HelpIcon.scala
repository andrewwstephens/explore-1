// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.components

import cats.syntax.all._
import crystal.react.implicits._
import eu.timepit.refined.types.string._
import explore.AppCtx
import explore.HelpContext
import explore.HelpCtx
import explore.Icons
import explore.components.ui.ExploreStyles
import explore.implicits._
import explore.model.Help
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common._

final case class HelpIcon(id: Help.Id) extends ReactProps[HelpIcon](HelpIcon.component) {}

object HelpIcon {
  type Props = HelpIcon

  type HelpId = NonEmptyFiniteString[20]

  val component = ScalaComponent
    .builder[Props]
    .stateless
    .render_P { p =>
      HelpCtx.usingView { help =>
        AppCtx.using { implicit ctx =>
          val helpMsg = help.zoom(HelpContext.displayedHelp)
          <.span(
            ^.onClick --> helpMsg.set(p.id.some).runAsyncCB,
            Icons.Info
              .link(true)
              .inverted(true)
              .clazz(ExploreStyles.HelpIcon)
          )
        }
      }
    }
    .build
}
