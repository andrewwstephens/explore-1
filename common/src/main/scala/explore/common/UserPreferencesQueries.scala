// Copyright (c) 2016-2021 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.common

import cats.MonadError
import cats.data.OptionT
import cats.effect.Effect
import cats.syntax.all._
import clue.GraphQLOperation
import clue.TransactionalClient
import clue.annotation.GraphQL
import clue.data.syntax._
import explore.model.GridLayoutSection
import explore.model.ResizableSection
import explore.model.layout._
import explore.schemas.UserPreferencesDB
import explore.schemas.UserPreferencesDB.Types._
import explore.schemas.implicits._
import lucuma.core.math.Angle
import lucuma.core.model.Target
import lucuma.core.model.User
import react.gridlayout.{BreakpointName => _, _}

import scala.collection.immutable.SortedMap

object UserPreferencesQueries {

  /**
   * Query to create a user, this is called when the app is started.
   * If the user exists the error is ignored
   */
  @GraphQL
  trait UserInsertMutationGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      mutation insert_user($id: String) {
        insert_lucuma_user_one(
          object: {
            user_id: $id
          },
          on_conflict: {
            update_columns: [],
            constraint: lucuma_user_pkey
          }
        ) {
          user_id
          }
        }
    """
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserInsertMutation extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        mutation insert_user($id: String) {
          insert_lucuma_user_one(
            object: {
              user_id: $id
            },
            on_conflict: {
              update_columns: [],
              constraint: lucuma_user_pkey
            }
          ) {
            user_id
            }
          }
      """
    case class Variables(val id: clue.data.Input[String] = clue.data.Ignore)
    object Variables {
      implicit val id: monocle.Lens[Variables, clue.data.Input[String]] = monocle.macros.GenLens[Variables](_.id)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val insert_lucuma_user_one: Option[Data.InsertLucumaUserOne] = None)
    object Data {
      case class InsertLucumaUserOne(val user_id: String)
      object InsertLucumaUserOne {
        implicit val user_id: monocle.Lens[Data.InsertLucumaUserOne, String] = monocle.macros.GenLens[Data.InsertLucumaUserOne](_.user_id)
        implicit val eqInsertLucumaUserOne: cats.Eq[Data.InsertLucumaUserOne] = cats.Eq.fromUniversalEquals
        implicit val showInsertLucumaUserOne: cats.Show[Data.InsertLucumaUserOne] = cats.Show.fromToString
        implicit val reuseInsertLucumaUserOne: japgolly.scalajs.react.Reusability[Data.InsertLucumaUserOne] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderInsertLucumaUserOne: io.circe.Decoder[Data.InsertLucumaUserOne] = io.circe.generic.semiauto.deriveDecoder[Data.InsertLucumaUserOne]
      }
      implicit val insert_lucuma_user_one: monocle.Lens[Data, Option[Data.InsertLucumaUserOne]] = monocle.macros.GenLens[Data](_.insert_lucuma_user_one)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def execute[F[_]](id: clue.data.Input[String] = clue.data.Ignore)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(id))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  /**
   * Update the width of a given area/user
   */
  @GraphQL
  trait UserWidthsCreationGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      mutation update_area_width($item: explore_resizable_width_insert_input!) {
        insert_explore_resizable_width_one(
          object: $item,
          on_conflict: {
            constraint: explore_resizable_width_pkey,
            update_columns: width
          }
        ) {
          user_id
          }
        }
   """
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserWidthsCreation extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        mutation update_area_width($item: explore_resizable_width_insert_input!) {
          insert_explore_resizable_width_one(
            object: $item,
            on_conflict: {
              constraint: explore_resizable_width_pkey,
              update_columns: width
            }
          ) {
            user_id
            }
          }
     """
    case class Variables(val item: ExploreResizableWidthInsertInput)
    object Variables {
      implicit val item: monocle.Lens[Variables, ExploreResizableWidthInsertInput] = monocle.macros.GenLens[Variables](_.item)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val insert_explore_resizable_width_one: Option[Data.InsertExploreResizableWidthOne] = None)
    object Data {
      case class InsertExploreResizableWidthOne(val user_id: String)
      object InsertExploreResizableWidthOne {
        implicit val user_id: monocle.Lens[Data.InsertExploreResizableWidthOne, String] = monocle.macros.GenLens[Data.InsertExploreResizableWidthOne](_.user_id)
        implicit val eqInsertExploreResizableWidthOne: cats.Eq[Data.InsertExploreResizableWidthOne] = cats.Eq.fromUniversalEquals
        implicit val showInsertExploreResizableWidthOne: cats.Show[Data.InsertExploreResizableWidthOne] = cats.Show.fromToString
        implicit val reuseInsertExploreResizableWidthOne: japgolly.scalajs.react.Reusability[Data.InsertExploreResizableWidthOne] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderInsertExploreResizableWidthOne: io.circe.Decoder[Data.InsertExploreResizableWidthOne] = io.circe.generic.semiauto.deriveDecoder[Data.InsertExploreResizableWidthOne]
      }
      implicit val insert_explore_resizable_width_one: monocle.Lens[Data, Option[Data.InsertExploreResizableWidthOne]] = monocle.macros.GenLens[Data](_.insert_explore_resizable_width_one)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def execute[F[_]](item: ExploreResizableWidthInsertInput)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(item))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class UserWidthsCreationOps(val self: UserWidthsCreation.type) extends AnyVal {
    import self._

    def storeWidthPreference[F[_]: Effect](
      userId:  Option[User.Id],
      section: ResizableSection,
      width:   Int
    )(implicit
      cl:      TransactionalClient[F, UserPreferencesDB]
    ): F[Unit] =
      userId.traverse { i =>
        execute[F](WidthUpsertInput(i, section, width)).attempt
      }.void
  }

  /**
   * Read the stored width of an area
   */
  @GraphQL
  trait UserAreaWidthsGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query area_width($user_id: String!, $section: resizable_area!) {
        explore_resizable_width_by_pk(
          section: $section,
          user_id: $user_id
        ) {
          width
        }
      }
    """
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserAreaWidths extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        query area_width($user_id: String!, $section: resizable_area!) {
          explore_resizable_width_by_pk(
            section: $section,
            user_id: $user_id
          ) {
            width
          }
        }
      """
    case class Variables(val user_id: String, val section: ResizableArea)
    object Variables {
      implicit val user_id: monocle.Lens[Variables, String] = monocle.macros.GenLens[Variables](_.user_id)
      implicit val section: monocle.Lens[Variables, ResizableArea] = monocle.macros.GenLens[Variables](_.section)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val explore_resizable_width_by_pk: Option[Data.ExploreResizableWidthByPk] = None)
    object Data {
      case class ExploreResizableWidthByPk(val width: Int)
      object ExploreResizableWidthByPk {
        implicit val width: monocle.Lens[Data.ExploreResizableWidthByPk, Int] = monocle.macros.GenLens[Data.ExploreResizableWidthByPk](_.width)
        implicit val eqExploreResizableWidthByPk: cats.Eq[Data.ExploreResizableWidthByPk] = cats.Eq.fromUniversalEquals
        implicit val showExploreResizableWidthByPk: cats.Show[Data.ExploreResizableWidthByPk] = cats.Show.fromToString
        implicit val reuseExploreResizableWidthByPk: japgolly.scalajs.react.Reusability[Data.ExploreResizableWidthByPk] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderExploreResizableWidthByPk: io.circe.Decoder[Data.ExploreResizableWidthByPk] = io.circe.generic.semiauto.deriveDecoder[Data.ExploreResizableWidthByPk]
      }
      implicit val explore_resizable_width_by_pk: monocle.Lens[Data, Option[Data.ExploreResizableWidthByPk]] = monocle.macros.GenLens[Data](_.explore_resizable_width_by_pk)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def query[F[_]](user_id: String, section: ResizableArea)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(user_id, section))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class UserAreaWidthsOps(val self: UserAreaWidths.type) extends AnyVal {
    import self._

    // Gets the width of a section.
    // This is coded to return a default in case
    // there is no data or errors
    def queryWithDefault[F[_]: MonadError[*[_], Throwable]](
      userId:       Option[User.Id],
      area:         ResizableSection,
      defaultValue: Int
    )(implicit cl:  TransactionalClient[F, UserPreferencesDB]): F[Int] =
      (for {
        uid <- OptionT.fromOption[F](userId)
        w   <-
          OptionT
            .liftF[F, Option[Int]] {
              query[F](uid.show, area.value)
                .map { r =>
                  r.explore_resizable_width_by_pk.map(_.width)
                }
                .recover(_ => none)
            }
      } yield w).value.map(_.flatten.getOrElse(defaultValue))
  }

  /**
   * Read the grid layout for a given section
   */
  @GraphQL
  trait ObsTabPreferencesQueryGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query
        obs_tab_preferences($user_id: String!, $criteria: grid_layout_positions_bool_exp!, $section: resizable_area!) {
          grid_layout_positions(where: $criteria) {
            breakpoint_name
            height
            width
            x
            y
            tile
          }
          explore_resizable_width_by_pk(
            section: $section,
            user_id: $user_id
          ) {
            width
          }
        }
    """
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object ObsTabPreferencesQuery extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        query
          obs_tab_preferences($user_id: String!, $criteria: grid_layout_positions_bool_exp!, $section: resizable_area!) {
            grid_layout_positions(where: $criteria) {
              breakpoint_name
              height
              width
              x
              y
              tile
            }
            explore_resizable_width_by_pk(
              section: $section,
              user_id: $user_id
            ) {
              width
            }
          }
      """
    case class Variables(val user_id: String, val criteria: GridLayoutPositionsBoolExp, val section: ResizableArea)
    object Variables {
      implicit val user_id: monocle.Lens[Variables, String] = monocle.macros.GenLens[Variables](_.user_id)
      implicit val criteria: monocle.Lens[Variables, GridLayoutPositionsBoolExp] = monocle.macros.GenLens[Variables](_.criteria)
      implicit val section: monocle.Lens[Variables, ResizableArea] = monocle.macros.GenLens[Variables](_.section)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val grid_layout_positions: List[Data.GridLayoutPositions], val explore_resizable_width_by_pk: Option[Data.ExploreResizableWidthByPk] = None)
    object Data {
      case class GridLayoutPositions(val breakpoint_name: BreakpointName, val height: Int, val width: Int, val x: Int, val y: Int, val tile: String)
      object GridLayoutPositions {
        implicit val breakpoint_name: monocle.Lens[Data.GridLayoutPositions, BreakpointName] = monocle.macros.GenLens[Data.GridLayoutPositions](_.breakpoint_name)
        implicit val height: monocle.Lens[Data.GridLayoutPositions, Int] = monocle.macros.GenLens[Data.GridLayoutPositions](_.height)
        implicit val width: monocle.Lens[Data.GridLayoutPositions, Int] = monocle.macros.GenLens[Data.GridLayoutPositions](_.width)
        implicit val x: monocle.Lens[Data.GridLayoutPositions, Int] = monocle.macros.GenLens[Data.GridLayoutPositions](_.x)
        implicit val y: monocle.Lens[Data.GridLayoutPositions, Int] = monocle.macros.GenLens[Data.GridLayoutPositions](_.y)
        implicit val tile: monocle.Lens[Data.GridLayoutPositions, String] = monocle.macros.GenLens[Data.GridLayoutPositions](_.tile)
        implicit val eqGridLayoutPositions: cats.Eq[Data.GridLayoutPositions] = cats.Eq.fromUniversalEquals
        implicit val showGridLayoutPositions: cats.Show[Data.GridLayoutPositions] = cats.Show.fromToString
        implicit val reuseGridLayoutPositions: japgolly.scalajs.react.Reusability[Data.GridLayoutPositions] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderGridLayoutPositions: io.circe.Decoder[Data.GridLayoutPositions] = io.circe.generic.semiauto.deriveDecoder[Data.GridLayoutPositions]
      }
      case class ExploreResizableWidthByPk(val width: Int)
      object ExploreResizableWidthByPk {
        implicit val width: monocle.Lens[Data.ExploreResizableWidthByPk, Int] = monocle.macros.GenLens[Data.ExploreResizableWidthByPk](_.width)
        implicit val eqExploreResizableWidthByPk: cats.Eq[Data.ExploreResizableWidthByPk] = cats.Eq.fromUniversalEquals
        implicit val showExploreResizableWidthByPk: cats.Show[Data.ExploreResizableWidthByPk] = cats.Show.fromToString
        implicit val reuseExploreResizableWidthByPk: japgolly.scalajs.react.Reusability[Data.ExploreResizableWidthByPk] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderExploreResizableWidthByPk: io.circe.Decoder[Data.ExploreResizableWidthByPk] = io.circe.generic.semiauto.deriveDecoder[Data.ExploreResizableWidthByPk]
      }
      implicit val grid_layout_positions: monocle.Lens[Data, List[Data.GridLayoutPositions]] = monocle.macros.GenLens[Data](_.grid_layout_positions)
      implicit val explore_resizable_width_by_pk: monocle.Lens[Data, Option[Data.ExploreResizableWidthByPk]] = monocle.macros.GenLens[Data](_.explore_resizable_width_by_pk)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def query[F[_]](user_id: String, criteria: GridLayoutPositionsBoolExp, section: ResizableArea)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(user_id, criteria, section))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class ObsTabPreferencesQueryOps(val self: ObsTabPreferencesQuery.type) extends AnyVal {
    import self._
    import UserPreferencesDB.Scalars._

    def positions2LayoutMap(
      g: (BreakpointName, List[Data.GridLayoutPositions])
    ): (react.gridlayout.BreakpointName, (Int, Int, Layout)) = {
      val bn = breakpointNameFromString(g._1)
      bn -> ((breakpointWidth(bn),
              breakpointCols(bn),
              Layout(
                g._2.map(p => LayoutItem(p.width, p.height, p.x, p.y, p.tile))
              )
             )
      )
    }
    // Gets the layout of a section.
    // This is coded to return a default in case
    // there is no data or errors
    def queryWithDefault[F[_]: MonadError[*[_], Throwable]](
      userId:        Option[User.Id],
      // layoutSection: GridLayoutSection,
      resizableArea: ResizableSection,
      defaultValue:  (Int, LayoutsMap)
    )(implicit cl:   TransactionalClient[F, UserPreferencesDB]): F[(Int, LayoutsMap)] =
      (for {
        uid <- OptionT.fromOption[F](userId)
        c   <-
          OptionT.pure(
            GridLayoutPositionsBoolExp(user_id = StringComparisonExp(uid.show.assign).assign)
          )
        r   <-
          OptionT
            .liftF[F, (Int, SortedMap[react.gridlayout.BreakpointName, (Int, Int, Layout)])] {
              query[F](uid.show, c, resizableArea.value).map { r =>
                (r.explore_resizable_width_by_pk.map(_.width), r.grid_layout_positions) match {
                  case (w, l) if l.isEmpty => (w.getOrElse(defaultValue._1), defaultValue._2)
                  case (w, l)              =>
                    (w.getOrElse(defaultValue._1),
                     SortedMap(l.groupBy(_.breakpoint_name).map(positions2LayoutMap).toList: _*)
                    )
                }
              }
            }
            .handleErrorWith(_ => OptionT.none)
      } yield r).getOrElse(defaultValue)
  }

  @GraphQL
  trait UserGridLayoutUpsertGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      mutation insert_layout_positions($objects: [grid_layout_positions_insert_input!]! = {}) {
        insert_grid_layout_positions(objects: $objects, on_conflict: {
          constraint: grid_layout_positions_pkey,
          update_columns: [width, height, x, y]
        }) {
          affected_rows
        }
      }"""
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserGridLayoutUpsert extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        mutation insert_layout_positions($objects: [grid_layout_positions_insert_input!]! = {}) {
          insert_grid_layout_positions(objects: $objects, on_conflict: {
            constraint: grid_layout_positions_pkey,
            update_columns: [width, height, x, y]
          }) {
            affected_rows
          }
        }"""
    case class Variables(val objects: List[GridLayoutPositionsInsertInput])
    object Variables {
      implicit val objects: monocle.Lens[Variables, List[GridLayoutPositionsInsertInput]] = monocle.macros.GenLens[Variables](_.objects)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val insert_grid_layout_positions: Option[Data.InsertGridLayoutPositions] = None)
    object Data {
      case class InsertGridLayoutPositions(val affected_rows: Int)
      object InsertGridLayoutPositions {
        implicit val affected_rows: monocle.Lens[Data.InsertGridLayoutPositions, Int] = monocle.macros.GenLens[Data.InsertGridLayoutPositions](_.affected_rows)
        implicit val eqInsertGridLayoutPositions: cats.Eq[Data.InsertGridLayoutPositions] = cats.Eq.fromUniversalEquals
        implicit val showInsertGridLayoutPositions: cats.Show[Data.InsertGridLayoutPositions] = cats.Show.fromToString
        implicit val reuseInsertGridLayoutPositions: japgolly.scalajs.react.Reusability[Data.InsertGridLayoutPositions] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderInsertGridLayoutPositions: io.circe.Decoder[Data.InsertGridLayoutPositions] = io.circe.generic.semiauto.deriveDecoder[Data.InsertGridLayoutPositions]
      }
      implicit val insert_grid_layout_positions: monocle.Lens[Data, Option[Data.InsertGridLayoutPositions]] = monocle.macros.GenLens[Data](_.insert_grid_layout_positions)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def execute[F[_]](objects: List[GridLayoutPositionsInsertInput])(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(objects))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class UserGridLayoutUpsertOps(val self: UserGridLayoutUpsert.type) extends AnyVal {
    import self._

    def storeLayoutsPreference[F[_]: Effect](
      userId:  Option[User.Id],
      section: GridLayoutSection,
      layouts: Layouts
    )(implicit
      cl:      TransactionalClient[F, UserPreferencesDB]
    ): F[Unit] =
      userId.traverse { uid =>
        execute[F](
          layouts.layouts.flatMap { bl =>
            bl.layout.l.collect {
              case i if i.i.nonEmpty =>
                GridLayoutPositionsInsertInput(
                  user_id = uid.show.assign,
                  section = section.value.assign,
                  breakpoint_name = bl.name.name.assign,
                  width = i.w.assign,
                  height = i.h.assign,
                  x = i.x.assign,
                  y = i.y.assign,
                  tile = i.i.getOrElse("").assign
                )
            }
          }
        ).attempt
      }.void
  }

  @GraphQL
  trait UserTargetPreferencesQueryGQL extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query target_preferences($user_id: String! = "", $targetId: String! = "") {
        lucuma_target_preferences_by_pk(targetId: $targetId, user_id: $user_id) {
          fov
        }
      }
    """
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserTargetPreferencesQuery extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """
        query target_preferences($user_id: String! = "", $targetId: String! = "") {
          lucuma_target_preferences_by_pk(targetId: $targetId, user_id: $user_id) {
            fov
          }
        }
      """
    case class Variables(val user_id: String, val targetId: String)
    object Variables {
      implicit val user_id: monocle.Lens[Variables, String] = monocle.macros.GenLens[Variables](_.user_id)
      implicit val targetId: monocle.Lens[Variables, String] = monocle.macros.GenLens[Variables](_.targetId)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val lucuma_target_preferences_by_pk: Option[Data.LucumaTargetPreferencesByPk] = None)
    object Data {
      case class LucumaTargetPreferencesByPk(val fov: Bigint)
      object LucumaTargetPreferencesByPk {
        implicit val fov: monocle.Lens[Data.LucumaTargetPreferencesByPk, Bigint] = monocle.macros.GenLens[Data.LucumaTargetPreferencesByPk](_.fov)
        implicit val eqLucumaTargetPreferencesByPk: cats.Eq[Data.LucumaTargetPreferencesByPk] = cats.Eq.fromUniversalEquals
        implicit val showLucumaTargetPreferencesByPk: cats.Show[Data.LucumaTargetPreferencesByPk] = cats.Show.fromToString
        implicit val reuseLucumaTargetPreferencesByPk: japgolly.scalajs.react.Reusability[Data.LucumaTargetPreferencesByPk] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderLucumaTargetPreferencesByPk: io.circe.Decoder[Data.LucumaTargetPreferencesByPk] = io.circe.generic.semiauto.deriveDecoder[Data.LucumaTargetPreferencesByPk]
      }
      implicit val lucuma_target_preferences_by_pk: monocle.Lens[Data, Option[Data.LucumaTargetPreferencesByPk]] = monocle.macros.GenLens[Data](_.lucuma_target_preferences_by_pk)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def query[F[_]](user_id: String, targetId: String)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(user_id, targetId))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class UserTargetPreferencesQueryOps(val self: UserTargetPreferencesQuery.type)
      extends AnyVal {
    import self._

    // Gets the layout of a section.
    // This is coded to return a default in case
    // there is no data or errors
    def queryWithDefault[F[_]: MonadError[*[_], Throwable]](
      uid:         User.Id,
      tid:         Target.Id,
      defaultFov:  Angle
    )(implicit cl: TransactionalClient[F, UserPreferencesDB]): F[Angle] =
      (for {
        r <-
          query[F](uid.show, tid.show)
            .map { r =>
              r.lucuma_target_preferences_by_pk.map(_.fov)
            }
            .handleError(_ => none)
      } yield r.map(Angle.fromMicroarcseconds)).map(_.getOrElse(defaultFov))
  }

  @GraphQL
  trait UserTargetPreferencesUpsertGQL extends GraphQLOperation[UserPreferencesDB] {
    val document =
      """mutation target_preferences_upsert($objects: lucuma_target_insert_input! = {}) {
        insert_lucuma_target(objects: [$objects], on_conflict: {constraint: lucuma_target_pkey, update_columns: targetId}) {
          affected_rows
        }
      }"""
  }

  /* BEGIN: Generated by clue. DO NOT remove or edit the following code or this comment. */
  // format: off
  object UserTargetPreferencesUpsert extends GraphQLOperation[UserPreferencesDB] {
    import UserPreferencesDB.Scalars._
    ignoreUnusedImportScalars()
    import UserPreferencesDB.Enums._
    ignoreUnusedImportEnums()
    import UserPreferencesDB.Types._
    ignoreUnusedImportTypes()
    val document = """mutation target_preferences_upsert($objects: lucuma_target_insert_input! = {}) {
          insert_lucuma_target(objects: [$objects], on_conflict: {constraint: lucuma_target_pkey, update_columns: targetId}) {
            affected_rows
          }
        }"""
    case class Variables(val objects: LucumaTargetInsertInput)
    object Variables {
      implicit val objects: monocle.Lens[Variables, LucumaTargetInsertInput] = monocle.macros.GenLens[Variables](_.objects)
      implicit val eqVariables: cats.Eq[Variables] = cats.Eq.fromUniversalEquals
      implicit val showVariables: cats.Show[Variables] = cats.Show.fromToString
      implicit val jsonEncoderVariables: io.circe.Encoder[Variables] = io.circe.generic.semiauto.deriveEncoder[Variables].mapJson(_.foldWith(clue.data.Input.dropIgnoreFolder))
    }
    case class Data(val insert_lucuma_target: Option[Data.InsertLucumaTarget] = None)
    object Data {
      case class InsertLucumaTarget(val affected_rows: Int)
      object InsertLucumaTarget {
        implicit val affected_rows: monocle.Lens[Data.InsertLucumaTarget, Int] = monocle.macros.GenLens[Data.InsertLucumaTarget](_.affected_rows)
        implicit val eqInsertLucumaTarget: cats.Eq[Data.InsertLucumaTarget] = cats.Eq.fromUniversalEquals
        implicit val showInsertLucumaTarget: cats.Show[Data.InsertLucumaTarget] = cats.Show.fromToString
        implicit val reuseInsertLucumaTarget: japgolly.scalajs.react.Reusability[Data.InsertLucumaTarget] = {
          import japgolly.scalajs.react.Reusability
          japgolly.scalajs.react.Reusability.derive
        }
        implicit val jsonDecoderInsertLucumaTarget: io.circe.Decoder[Data.InsertLucumaTarget] = io.circe.generic.semiauto.deriveDecoder[Data.InsertLucumaTarget]
      }
      implicit val insert_lucuma_target: monocle.Lens[Data, Option[Data.InsertLucumaTarget]] = monocle.macros.GenLens[Data](_.insert_lucuma_target)
      implicit val eqData: cats.Eq[Data] = cats.Eq.fromUniversalEquals
      implicit val showData: cats.Show[Data] = cats.Show.fromToString
      implicit val reuseData: japgolly.scalajs.react.Reusability[Data] = {
        import japgolly.scalajs.react.Reusability
        japgolly.scalajs.react.Reusability.derive
      }
      implicit val jsonDecoderData: io.circe.Decoder[Data] = io.circe.generic.semiauto.deriveDecoder[Data]
    }
    val varEncoder: io.circe.Encoder[Variables] = Variables.jsonEncoderVariables
    val dataDecoder: io.circe.Decoder[Data] = Data.jsonDecoderData
    def execute[F[_]](objects: LucumaTargetInsertInput)(implicit client: clue.TransactionalClient[F, UserPreferencesDB]) = client.request(this)(Variables(objects))
  }
  // format: on
  /* END: Generated by clue. Will be replaced when regenerating. */

  implicit class UserTargetPreferencesUpsertOps(val self: UserTargetPreferencesUpsert.type)
      extends AnyVal {
    import self._
    import UserPreferencesDB.Enums._

    def updateFov[F[_]: Effect](
      uid:      User.Id,
      targetId: Target.Id,
      fov:      Angle
    )(implicit
      cl:       TransactionalClient[F, UserPreferencesDB]
    ): F[Unit] =
      execute[F](
        LucumaTargetInsertInput(
          target_id = targetId.show.assign,
          lucuma_target_preferences = LucumaTargetPreferencesArrRelInsertInput(
            data = List(
              LucumaTargetPreferencesInsertInput(user_id = uid.show.assign,
                                                 fov = fov.toMicroarcseconds.assign
              )
            ),
            on_conflict = LucumaTargetPreferencesOnConflict(
              constraint = LucumaTargetPreferencesConstraint.LucumaTargetPreferencesPkey,
              update_columns = List(LucumaTargetPreferencesUpdateColumn.Fov)
            ).assign
          ).assign
        )
      ).attempt.void
  }
}
