// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore

import cats.Order.*
import cats.data.NonEmptySet
import cats.syntax.all.*
import crystal.react.View
import explore.components.ui.ExploreStyles
import explore.config.sequence.SequenceEditor
import explore.model.Page
import explore.model.Page.*
import explore.model.*
import explore.programs.ProgramsPopup
import explore.proposal.ProposalTabContents
import explore.tabs.*
import japgolly.scalajs.react.ReactMonocle.*
import japgolly.scalajs.react.extra.router.*
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^.*
import lucuma.core.model.Observation
import lucuma.core.model.Program
import lucuma.core.model.Target
import lucuma.core.util.Gid
import lucuma.ui.syntax.all.given

import scala.collection.immutable.SortedSet
import scala.scalajs.LinkingInfo
import scala.util.Random

sealed trait ElementItem  extends Product with Serializable
case object IconsElement  extends ElementItem
case object LabelsElement extends ElementItem

object Routing:

  private def homeTab(): VdomElement = UnderConstruction()

  private def targetTab(page: Page, model: View[RootModel]): VdomElement =
    val routingInfo = RoutingInfo.from(page)
    TargetTabContents(
      model.zoom(RootModel.userId).get,
      routingInfo.programId,
      routingInfo.focused,
      model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forAsterismGroupList),
      model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forSiderealTarget),
      model.zoom(RootModel.searchingTarget),
      model.zoom(RootModel.expandedIds.andThen(ExpandedIds.asterismObsIds))
    )

  private def obsTab(page: Page, model: View[RootModel]): VdomElement =
    val routingInfo = RoutingInfo.from(page)
    ObsTabContents(
      model.zoom(RootModel.userId).get,
      routingInfo.programId,
      routingInfo.focused,
      model.zoom(RootModel.undoStacks),
      model.zoom(RootModel.searchingTarget)
    )

  private def constraintSetTab(page: Page, model: View[RootModel]): VdomElement =
    val routingInfo = RoutingInfo.from(page)
    ConstraintsTabContents(
      model.zoom(RootModel.userId).get,
      routingInfo.programId,
      routingInfo.focused.obsSet,
      model.zoom(RootModel.expandedIds.andThen(ExpandedIds.constraintSetObsIds)),
      model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forConstraintList),
      model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forConstraintGroup)
    )

  private def proposalTab(page: Page, model: View[RootModel]): VdomElement =
    val routingInfo = RoutingInfo.from(page)
    ProposalTabContents(routingInfo.programId,
                        model.zoom(RootModel.user).get,
                        model.zoom(RootModel.undoStacks).zoom(ModelUndoStacks.forProposal)
    )

  private def configurationsTab(page: Page): VdomElement =
    SequenceEditor(RoutingInfo.from(page).programId)

  private def showProgramSelectionPopup(model: View[RootModel]): VdomElement =
    ProgramsPopup(
      currentProgramId = none,
      undoStacks = model.zoom(RootModel.undoStacks)
    )

  def config: RouterWithPropsConfig[Page, View[RootModel]] =
    RouterWithPropsConfigDsl[Page, View[RootModel]].buildConfig { dsl =>
      import dsl.*

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
          | staticRoute(root, NoProgramPage) ~> renderP(showProgramSelectionPopup _)

          | dynamicRouteCT((root / id[Program.Id]).xmapL(HomePage.iso)) ~> dynRenderP {
            case (_, _) => homeTab()
          }

          | dynamicRouteCT(
            (root / id[Program.Id] / "proposal").xmapL(ProposalPage.iso)
          ) ~> dynRenderP { case (p, m) => proposalTab(p, m) }

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
          .renderWithP((_, resolution) => ExploreLayout(resolution))
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
