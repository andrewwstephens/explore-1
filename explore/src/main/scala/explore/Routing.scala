// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore

import cats.Order._
import cats.data.NonEmptySet
import cats.syntax.all._
import crystal.react.ReuseView
import explore.components.ui.ExploreStyles
import explore.config.SequenceEditor
import explore.model.Page
import explore.model.Page._
import explore.model._
import explore.proposal._
import explore.tabs._
import japgolly.scalajs.react.ReactMonocle._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import lucuma.core.model.Observation
import lucuma.core.model.Program
import lucuma.core.model.Target
import lucuma.core.util.Gid
import react.resizeDetector.ResizeDetector

import scala.collection.immutable.SortedSet
import scala.scalajs.LinkingInfo
import scala.util.Random

sealed trait ElementItem  extends Product with Serializable
case object IconsElement  extends ElementItem
case object LabelsElement extends ElementItem

object Routing {

  private def withSize(f: ResizeDetector.Dimensions => VdomElement): VdomElement =
    ResizeDetector() { s =>
      <.div(ExploreStyles.SizeDetector, s.targetRef)(
        f(s)
      )
    }

  private def homeTab(): VdomElement = UnderConstruction()

  private def targetTab(page: Page, model: ReuseView[RootModel]): VdomElement =
    AppCtx.using { implicit ctx =>
      val routingInfo = RoutingInfo.from(page)
      TargetTabContents(
        model.zoom(RootModel.userId).get,
        routingInfo.programId,
        routingInfo.focusedObsSet,
        routingInfo.focusedTarget,
        model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forAsterismGroupList),
        model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forSiderealTarget),
        model.zoom(RootModel.searchingTarget),
        model.zoom(RootModel.expandedIds.andThen(ExpandedIds.asterismObsIds)),
        model.zoom(RootModel.targetSummaryHiddenColumns)
      )
    }

  private def obsTab(page: Page, model: ReuseView[RootModel]): VdomElement =
    AppCtx.using { implicit ctx =>
      val routingInfo = RoutingInfo.from(page)
      ObsTabContents(
        model.zoom(RootModel.userId),
        routingInfo.programId,
        routingInfo.focusedObsSet.map(_.head),
        routingInfo.focusedTarget,
        model.zoom(RootModel.undoStacks),
        model.zoom(RootModel.searchingTarget),
        model.zoom(RootModel.targetSummaryHiddenColumns)
      )
    }

  private def constraintSetTab(page: Page, model: ReuseView[RootModel]): VdomElement =
    withSize(size =>
      AppCtx.using { implicit ctx =>
        val routingInfo = RoutingInfo.from(page)
        ConstraintSetTabContents(
          model.zoom(RootModel.userId).get,
          routingInfo.programId,
          routingInfo.focusedObsSet,
          model.zoom(
            RootModel.expandedIds.andThen(ExpandedIds.constraintSetObsIds)
          ),
          model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forConstraintList),
          model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forConstraintGroup),
          model.zoom(RootModel.constraintSummaryHiddenColumns),
          model.zoom(RootModel.constraintSummarySorting),
          size
        )
      }
    )

  private def proposalTab(): VdomElement =
    AppCtx.using { implicit ctx =>
      ProposalTabContents()
    }

  private def configurationsTab(page: Page): VdomElement =
    SequenceEditor(RoutingInfo.from(page).programId)

  def config: RouterWithPropsConfig[Page, ReuseView[RootModel]] =
    RouterWithPropsConfigDsl[Page, ReuseView[RootModel]].buildConfig { dsl =>
      import dsl._

      // We can't use gid.regexPattern because capture groups don't work here.
      def gidRegEx[Id](implicit gid: Gid[Id]): String =
        s"${gid.tag.value.toString}-[1-9a-f][0-9a-f]*"

      def id[Id](implicit gid: Gid[Id]): StaticDsl.RouteB[Id] =
        string(gidRegEx).pmapL(gid.fromString)

      def idList[Id](implicit gid: Gid[Id]): StaticDsl.RouteB[NonEmptySet[Id]] = {
        val separator = ":"
        val regex     = s"${gidRegEx[Id]}(?:\\$separator${gidRegEx[Id]})*"
        string(regex).pmap(
          _.split(s"\\$separator").toList
            .map(s => gid.fromString.getOption(s))
            .sequence
            .map(l => NonEmptySet.fromSetUnsafe(SortedSet.from(l)))
        )(_.toList.map(_.toString).mkString(separator))
      }

      val rules =
        (emptyRule
        // temporarily redirect to p-2 until we have program selection available.
          | staticRoute(root, NoProgramPage) ~> redirectToPath("/p-2")(SetRouteVia.HistoryReplace)
          | dynamicRouteCT((root / id[Program.Id]).xmapL(HomePage.iso)) ~> dynRenderP {
            case (_, _) => homeTab()
          }

          | dynamicRouteCT(
            (root / id[Program.Id] / "proposal").xmapL(ProposalPage.iso)
          ) ~> dynRenderP { case (_, _) => proposalTab() }

          | dynamicRouteCT(
            (root / id[Program.Id] / "observations").xmapL(ObservationsBasePage.iso)
          ) ~> dynRenderP { case (p, m) => obsTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "observation" / id[Observation.Id] / "target" / id[Target.Id])
              .xmapL(ObsTargetPage.iso)
          ) ~> dynRenderP { case (p, m) => obsTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "observation" / id[Observation.Id]).xmapL(ObsPage.iso)
          ) ~> dynRenderP { case (p, m) => obsTab(p, m) }

          | dynamicRouteCT((root / id[Program.Id] / "targets").xmapL(TargetsBasePage.iso)) ~>
          dynRenderP { case (p, m) => targetTab(p, m) }

          | dynamicRouteCT((root / id[Program.Id] / "target" / id[Target.Id]).xmapL(TargetPage.iso))
          ~> dynRenderP { case (p, m) => targetTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "targets/obs" / idList[Observation.Id] / "target" / id[
              Target.Id
            ])
              .xmapL(TargetWithObsPage.iso)
          ) ~> dynRenderP { case (p, m) => targetTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "targets/obs" / idList[Observation.Id])
              .xmapL(TargetsObsPage.iso)
          ) ~> dynRenderP { case (p, m) => targetTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "configurations").xmapL(ConfigurationsPage.iso)
          ) ~> dynRenderP { case (p, _) => configurationsTab(p) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "constraints").xmapL(ConstraintsBasePage.iso)
          ) ~> dynRenderP { case (p, m) => constraintSetTab(p, m) }

          | dynamicRouteCT(
            (root / id[Program.Id] / "constraints/obs" / idList[Observation.Id])
              .xmapL(ConstraintsObsPage.iso)
          ) ~> dynRenderP { case (p, m) => constraintSetTab(p, m) })

      val configuration =
        rules
          .notFound(redirectToPage(NoProgramPage)(SetRouteVia.HistoryPush))
          .renderWithP(layout)
      // .logToConsole

      // Only link and run this in dev mode. Works since calling `verify` trigger verification immediately.
      if (LinkingInfo.developmentMode) {
        def randomId[Id](fromLong: Long => Option[Id]): Id =
          fromLong(Random.nextLong().abs).get

        def pid      = randomId(Program.Id.fromLong)
        def oid      = randomId(Observation.Id.fromLong)
        def tid      = randomId(Target.Id.fromLong)
        def oneObs   = ObsIdSet.one(oid)
        def twoObs   = oneObs.add(oid)
        def threeObs = oneObs.add(oid)

        configuration
          .verify(
            NoProgramPage,
            HomePage(pid),
            ProposalPage(pid),
            ObservationsBasePage(pid),
            ObsPage(pid, oid),
            ObsTargetPage(pid, oid, tid),
            TargetsBasePage(pid),
            TargetWithObsPage(pid, oneObs, tid),
            TargetWithObsPage(pid, twoObs, tid),
            TargetWithObsPage(pid, threeObs, tid),
            TargetsObsPage(pid, oneObs),
            TargetsObsPage(pid, twoObs),
            TargetsObsPage(pid, threeObs),
            TargetPage(pid, tid),
            ConfigurationsPage(pid),
            ConstraintsBasePage(pid),
            ConstraintsObsPage(pid, oneObs),
            ConstraintsObsPage(pid, twoObs),
            ConstraintsObsPage(pid, threeObs)
          )
      }

      configuration
    }

  private def layout(
    c: RouterCtl[Page],
    r: ResolutionWithProps[Page, ReuseView[RootModel]]
  ): ReuseView[RootModel] => VdomElement =
    ExploreLayout(c, r)
}
