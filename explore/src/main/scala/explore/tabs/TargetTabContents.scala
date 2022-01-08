// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.tabs

import cats.Order._
import cats.effect.IO
import cats.syntax.all._
import crystal.react.View
import crystal.react.hooks._
import crystal.react.implicits._
import crystal.react.reuse._
import eu.timepit.refined.auto._
import explore.Icons
import explore.common.AsterismQueries._
import explore.common.UserPreferencesQueries._
import explore.common.UserPreferencesQueriesGQL._
import explore.components.Tile
import explore.components.ui.ExploreStyles
import explore.implicits._
import explore.model._
import explore.model.enum.AppTab
import explore.model.reusability._
import explore.observationtree.AsterismGroupObsList
import explore.syntax.ui._
import explore.targeteditor.AsterismEditor
import explore.undo._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import lucuma.core.model.SiderealTarget
import lucuma.core.model.Target
import lucuma.core.model.User
import lucuma.ui.reusability._
import lucuma.ui.utils._
import org.scalajs.dom.window
import react.common._
import react.common.implicits._
import react.draggable.Axis
import react.resizable._
import react.resizeDetector.UseResizeDetectorProps
import react.resizeDetector._
import react.resizeDetector.hooks._
import react.semanticui.elements.button.Button
import react.semanticui.elements.button.Button.ButtonProps
import react.semanticui.sizes._

import scala.collection.immutable.SortedSet
import scala.concurrent.duration._

final case class TargetTabContents(
  userId:            Option[User.Id],
  focusedObs:        View[Option[FocusedObs]],
  listUndoStacks:    View[UndoStacks[IO, AsterismGroupList]],
  targetsUndoStacks: View[Map[Target.Id, UndoStacks[IO, SiderealTarget]]],
  searching:         View[Set[Target.Id]],
  expandedIds:       View[SortedSet[ObsIdSet]],
  hiddenColumns:     View[Set[String]]
)(implicit val ctx:  AppContextIO)
    extends ReactFnProps[TargetTabContents](TargetTabContents.component)

object TargetTabContents {
  type Props = TargetTabContents

  implicit val propsReuse: Reusability[Props] = Reusability.derive

  val treeWidthLens = TwoPanelState.treeWidth[ObsIdSet]
  val selectedLens  = TwoPanelState.selected[ObsIdSet]

  protected def renderFn(
    props:                Props,
    panelState:           View[TwoPanelState[ObsIdSet]],
    options:              View[TargetVisualOptions],
    resize:               UseResizeDetectorReturn,
    asterismGroupWithObs: View[AsterismGroupsWithObs]
  )(implicit ctx:         AppContextIO): VdomNode = {
    val treeResize =
      (_: ReactEvent, d: ResizeCallbackData) =>
        panelState.zoom(treeWidthLens).set(d.size.width) *>
          (UserWidthsCreation
            .storeWidthPreference[IO](props.userId, ResizableSection.TargetsTree, d.size.width))
            .runAsync
            .debounce(1.second)

    val treeWidth    = panelState.get.treeWidth.toInt
    val selectedView = panelState.zoom(selectedLens)

    val targetMap = asterismGroupWithObs.get.targetGroups

    // Tree area
    def tree(objectsWithObs: View[AsterismGroupsWithObs]) =
      <.div(^.width := treeWidth.px, ExploreStyles.Tree |+| ExploreStyles.ResizableSinglePanel)(
        treeInner(objectsWithObs)
      )

    def treeInner(objectsWithObs: View[AsterismGroupsWithObs]) =
      <.div(ExploreStyles.TreeBody)(
        AsterismGroupObsList(
          objectsWithObs,
          props.focusedObs,
          selectedView,
          props.expandedIds,
          props.listUndoStacks
        )
      )

    def findAsterismGroup(
      obsIds: ObsIdSet,
      agl:    AsterismGroupList
    ): Option[AsterismGroup] = agl.values.find(_.obsIds.intersects(obsIds))

    def onModAsterismsWithObs(
      groupIds:  ObsIdSet,
      editedIds: ObsIdSet
    )(agwo:      AsterismGroupsWithObs): Callback =
      findAsterismGroup(editedIds, agwo.asterismGroups).foldMap { tlg =>
        // We should always find the group.
        // If a group was edited while closed and it didn't create a merger, keep it closed,
        // otherwise expand all affected groups.
        props.expandedIds
          .mod { eids =>
            val withOld       =
              if (groupIds === editedIds) eids
              else eids + groupIds.removeUnsafe(editedIds)
            val withOldAndNew =
              if (editedIds === tlg.obsIds && editedIds === groupIds) withOld
              else withOld + tlg.obsIds

            withOldAndNew.filter(ids => agwo.asterismGroups.contains(ids)) // clean up
          }
      }

    val backButton = Reuse.always[VdomNode](
      Button(
        as = <.a,
        size = Mini,
        compact = true,
        basic = true,
        clazz = ExploreStyles.TileBackButton |+| ExploreStyles.BlendedButton,
        onClickE = linkOverride[ButtonProps](
          selectedView.set(SelectedPanel.tree)
        )
      )(^.href := ctx.pageUrl(AppTab.Targets, none), Icons.ChevronLeft)
    )

    /**
     * Render the summary table.
     */
    def renderSummary: VdomNode =
      Tile("asterismSummary", "Asterism Summary", backButton.some)(
        Reuse.by( // TODO Add reuseCurrying for higher arities in crystal
          (asterismGroupWithObs.get, props.hiddenColumns, props.focusedObs, props.expandedIds)
        )((_: Tile.RenderInTitle) => explore.UnderConstruction())
        // TODO: Fix the summary table and reinstate
        // )((renderInTitle: Tile.RenderInTitle) =>
        //   TargetSummaryTable(asterismGroupWithObs.get,
        //                      props.hiddenColumns,
        //                      selectedView,
        //                      props.focusedObs,
        //                      props.expandedIds,
        //                      renderInTitle
        //   ): VdomNode
        // )
      )

    /**
     * Render the asterism editor
     *
     * @param idsToEdit
     *   The observations to include in the edit. This needs to be a subset of the ids in
     *   asterismGroup
     * @param asterismGroup
     *   The AsterismGroup that is the basis for editing. All or part of it may be included in the
     *   edit.
     */
    def renderEditor(idsToEdit: ObsIdSet, asterismGroup: AsterismGroup): VdomNode = {
      val focusedObs = props.focusedObs.get
      val groupIds   = asterismGroup.obsIds
      val targetIds  = asterismGroup.targetIds

      val asterism = targetIds.toList.map(targetMap.get).flatten

      val getAsterism: AsterismGroupsWithObs => List[TargetWithId] = _ => asterism
      def modAsterism(
        mod: List[TargetWithId] => List[TargetWithId]
      ): AsterismGroupsWithObs => AsterismGroupsWithObs = agwo => {
        val asterismGroups      = agwo.asterismGroups
        val targetGroups        = agwo.targetGroups
        val moddedAsterism      = mod(asterism)
        val newTargetIds        = SortedSet.from(moddedAsterism.map(TargetWithId.id.get))
        // make sure any added targets are in the map and update modified ones.
        val updatedTargetGroups = targetGroups ++ moddedAsterism.map(twi => (twi._1, twi))

        val splitAsterisms =
          if (targetIds === newTargetIds)
            asterismGroups
          else if (idsToEdit === groupIds) {
            asterismGroups + asterismGroup.copy(targetIds = newTargetIds).asObsKeyValue
          } else {
            // Since we're editing a subgroup, actions such as adding/removing a target will result in a split
            asterismGroups - groupIds +
              asterismGroup.removeObsIdsUnsafe(idsToEdit).asObsKeyValue +
              AsterismGroup(idsToEdit, newTargetIds).asObsKeyValue
          }

        // see if the edit caused a merger - note that we're searching the original lists.
        val oMergeWithAg = asterismGroups.find { case (obsIds, ag) =>
          obsIds =!= groupIds && ag.targetIds === newTargetIds
        }

        val updatedAsterismGroups = oMergeWithAg.fold(
          splitAsterisms
        ) { mergeWithAg =>
          splitAsterisms - idsToEdit - mergeWithAg._1 +
            mergeWithAg._2.addObsIds(groupIds).asObsKeyValue
        }

        agwo.copy(asterismGroups = updatedAsterismGroups, targetGroups = updatedTargetGroups)
      }

      val asterismView: View[List[TargetWithId]] = asterismGroupWithObs
        .withOnMod(onModAsterismsWithObs(groupIds, idsToEdit))
        .zoom(getAsterism)(modAsterism)

      val title = focusedObs match {
        case Some(FocusedObs(id)) => s"Observation $id"
        case None                 =>
          s"Editing ${idsToEdit.size} Asterisms"
      }

      Tile("targetEditor", title, backButton.some)(
        Reuse
          .by(
            (props.userId,
             idsToEdit,
             asterismView,
             props.targetsUndoStacks,
             props.searching,
             options,
             props.hiddenColumns
            )
          )((renderInTitle: Tile.RenderInTitle) =>
            props.userId.map(uid =>
              <.div(
                AsterismEditor(uid,
                               idsToEdit,
                               asterismView,
                               props.targetsUndoStacks,
                               props.searching,
                               options,
                               props.hiddenColumns,
                               renderInTitle
                )
              )
            ): VdomNode
          )
          .reuseAlways
      )
    }

    val coreWidth  = resize.width.getOrElse(0) - treeWidth
    val coreHeight = resize.height.getOrElse(0)

    val selectedPanel = panelState.get.selected
    val rightSide     = selectedPanel.optValue
      .flatMap(ids =>
        findAsterismGroup(ids, asterismGroupWithObs.get.asterismGroups).map(ag => (ids, ag))
      )
      .fold[VdomNode](renderSummary) { case (idsToEdit, asterismGroup) =>
        renderEditor(idsToEdit, asterismGroup)
      }

    // It would be nice to make a single component here but it gets hard when you
    // have the resizable element. Instead we have either two panels with a resizable
    // or only one panel at a time (Mobile)
    val body = if (window.canFitTwoPanels) {
      <.div(
        ExploreStyles.TreeRGL,
        <.div(ExploreStyles.Tree, treeInner(asterismGroupWithObs))
          .when(selectedPanel.leftPanelVisible),
        <.div(^.key := "target-right-side", ExploreStyles.SinglePanelTile)(
          rightSide
        ).when(selectedPanel.rightPanelVisible)
      )
    } else {
      <.div(
        ExploreStyles.TreeRGL,
        Resizable(
          axis = Axis.X,
          width = treeWidth,
          height = coreHeight,
          minConstraints = (Constants.MinLeftPanelWidth.toInt, 0),
          maxConstraints = (coreWidth / 2, 0),
          onResize = treeResize,
          resizeHandles = List(ResizeHandleAxis.East),
          content = tree(asterismGroupWithObs),
          clazz = ExploreStyles.ResizableSeparator
        ),
        <.div(^.key   := "target-right-side",
              ExploreStyles.SinglePanelTile,
              ^.width := coreWidth.px,
              ^.left  := treeWidth.px
        )(
          rightSide
        )
      )
    }
    body.withRef(resize.ref)
  }

  protected val component =
    ScalaFnComponent
      .withHooks[Props]
      .useStateView(TwoPanelState.initial[ObsIdSet](SelectedPanel.Uninitialized))
      .useStateView(TargetVisualOptions.Default)
      .useEffectOnMountBy { (props, panels, _) =>
        implicit val ctx = props.ctx
        UserAreaWidths
          .queryWithDefault[IO](props.userId,
                                ResizableSection.TargetsTree,
                                Constants.InitialTreeWidth.toInt
          )
          .attempt
          .flatMap {
            case Right(w) =>
              panels
                .zoom(TwoPanelState.treeWidth[ObsIdSet])
                .set(w)
                .to[IO]
            case Left(_)  => IO.unit
          }
          .runAsync
      }
      .useResizeDetector(UseResizeDetectorProps())
      .renderWithReuse { (props, tps, opts, resize) =>
        implicit val ctx = props.ctx
        AsterismGroupLiveQuery(
          Reuse(renderFn _)(props, tps, opts, resize)
        )
      }

}
