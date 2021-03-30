// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.tabs

import cats.effect.IO
import cats.syntax.all._
import crystal.react.implicits._
import explore.AppCtx
import explore.Icons
import explore.common.UserPreferencesQueries._
import explore.components.Tile
import explore.components.TileButton
import explore.components.ui.ExploreStyles
import explore.constraints.ConstraintSetEditor
import explore.implicits._
import explore.model._
import explore.model.Focused._
import explore.model.enum.AppTab
import explore.model.reusability._
import explore.observationtree.ConstraintSetObsList
import explore.observationtree.ConstraintSetObsQueries._
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.vdom.html_<^._
import lucuma.core.model.ConstraintSet
import lucuma.core.model.User
import lucuma.ui.reusability._
import lucuma.ui.utils._
import org.scalajs.dom.window
import react.common._
import react.common.implicits._
import react.draggable.Axis
import react.resizable._
import react.resizeDetector.ResizeDetector
import react.semanticui.elements.button.Button
import react.semanticui.elements.button.Button.ButtonProps
import react.semanticui.sizes._

import scala.collection.immutable.SortedSet
import scala.concurrent.duration._

final case class ConstraintSetTabContents(
  userId:      ViewOpt[User.Id],
  focused:     View[Option[Focused]],
  expandedIds: View[SortedSet[ConstraintSet.Id]],
  size:        ResizeDetector.Dimensions
) extends ReactProps[ConstraintSetTabContents](ConstraintSetTabContents.component) {
  def isCsSelected: Boolean = focused.get.collect { case Focused.FocusedConstraintSet(_) =>
    ()
  }.isDefined
}

object ConstraintSetTabContents {
  type Props = ConstraintSetTabContents
  type State = TwoPanelState

  implicit val propsReuse: Reusability[Props] = Reusability.derive

  def readWidthPreference($ : ComponentDidMount[Props, State, Unit]): Callback =
    AppCtx.runWithCtx { implicit ctx =>
      (UserAreaWidths.queryWithDefault[IO]($.props.userId.get,
                                           ResizableSection.ConstraintSetsTree,
                                           Constants.InitialTreeWidth.toInt
      ) >>= $.setStateLIn[IO](TwoPanelState.treeWidth)).runAsyncCB
    }

  protected val component =
    ScalaComponent
      .builder[Props]
      .getDerivedStateFromPropsAndState((p, s: Option[State]) =>
        s match {
          case None    => TwoPanelState.initial(p.isCsSelected)
          case Some(s) =>
            if (s.elementSelected =!= p.isCsSelected) s.copy(elementSelected = p.isCsSelected)
            else s
        }
      )
      .renderPS { ($, props, state) =>
        AppCtx.runWithCtx { implicit ctx =>
          val treeResize =
            (_: ReactEvent, d: ResizeCallbackData) =>
              ($.setStateLIn[IO](TwoPanelState.treeWidth)(d.size.width) *>
                UserWidthsCreation
                  .storeWidthPreference[IO](props.userId.get,
                                            ResizableSection.ConstraintSetsTree,
                                            d.size.width
                  )).runAsyncCB
                .debounce(1.second)

          val treeWidth = state.treeWidth.toInt

          def tree(constraintSetsWithObs: View[ConstraintSetsWithObs]) =
            <.div(^.width := treeWidth.px,
                  ExploreStyles.Tree |+| ExploreStyles.ResizableSinglePanel
            )(treeInner(constraintSetsWithObs))

          def treeInner(constraintSetsWithObs: View[ConstraintSetsWithObs]) =
            <.div(ExploreStyles.TreeBody)(
              ConstraintSetObsList(constraintSetsWithObs, props.focused, props.expandedIds)
            )

          val backButton = TileButton(
            Button(
              as = <.a,
              size = Mini,
              compact = true,
              basic = true,
              clazz = ExploreStyles.TileBackButton |+| ExploreStyles.BlendedButton,
              onClickE = linkOverride[IO, ButtonProps](props.focused.set(none))
            )(^.href := ctx.pageUrl(AppTab.Constraints, none), Icons.ChevronLeft.fitted(true))
          )

          ConstraintSetObsLiveQuery { constraintSetsWithObs =>
            val csIdOpt = props.focused.get.collect {
              case FocusedConstraintSet(csId) => csId.some
              case FocusedObs(obsId)          =>
                constraintSetsWithObs.get.obs.getElement(obsId).flatMap(_.constraints).map(_.id)
            }.flatten

            val coreWidth  = props.size.width.getOrElse(0) - treeWidth
            val coreHeight = props.size.height.getOrElse(0)

            val rightSide = Tile("Constraints", backButton.some)(
              csIdOpt.map(csId => ConstraintSetEditor(csId).withKey(csId.show))
            )

            if (window.innerWidth <= Constants.TwoPanelCutoff) {
              <.div(
                ExploreStyles.TreeRGL,
                <.div(ExploreStyles.Tree, treeInner(constraintSetsWithObs))
                  .when(state.leftPanelVisible),
                <.div(^.key := "constraintset-right-side", ExploreStyles.SinglePanelTile)(
                  rightSide
                ).when(state.rightPanelVisible)
              )
            } else {
              <.div(
                ExploreStyles.TreeRGL,
                Resizable(
                  axis = Axis.X,
                  width = treeWidth,
                  height = coreHeight,
                  minConstraints = (Constants.MinLeftPanelWidth.toInt, 0),
                  maxConstraints = (props.size.width.getOrElse(0) / 2, 0),
                  onResize = treeResize,
                  resizeHandles = List(ResizeHandleAxis.East),
                  content = tree(constraintSetsWithObs),
                  clazz = ExploreStyles.ResizableSeparator
                ),
                <.div(^.key := "constraintset-right-side",
                      ExploreStyles.SinglePanelTile,
                      ^.width := coreWidth.px,
                      ^.left := treeWidth.px
                )(
                  rightSide
                )
              )
            }
          }
        }
      }
      .componentDidMount(readWidthPreference)
      .configure(Reusability.shouldComponentUpdate)
      .build
}
