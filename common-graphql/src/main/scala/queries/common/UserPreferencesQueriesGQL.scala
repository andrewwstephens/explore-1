// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package queries.common

import clue.GraphQLOperation
import clue.annotation.GraphQL
import queries.schemas.UserPreferencesDB

object UserPreferencesQueriesGQL {

  /**
   * Query to create a user, this is called when the app is started. If the user exists the error is
   * ignored
   */
  @GraphQL
  trait UserInsertMutation extends GraphQLOperation[UserPreferencesDB] {
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

  /**
   * Update the width of a given area/user
   */
  @GraphQL
  trait UserWidthsCreation extends GraphQLOperation[UserPreferencesDB] {
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

  /**
   * Read the stored width of an area
   */
  @GraphQL
  trait UserAreaWidths extends GraphQLOperation[UserPreferencesDB] {
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

  /**
   * Read the grid layout for a given section
   */
  @GraphQL
  trait UserGridLayoutQuery extends GraphQLOperation[UserPreferencesDB] {
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

  @GraphQL
  trait UserGridLayoutUpsert extends GraphQLOperation[UserPreferencesDB] {
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

  @GraphQL
  trait UserTargetPreferencesQuery extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query target_preferences($user_id: String! = "", $targetId: String! = "") {
        lucuma_target_preferences_by_pk(target_id: $targetId, user_id: $user_id) {
          fovRA
          fovDec
          viewOffsetP
          viewOffsetQ
          agsCandidates
          agsOverlay
          fullScreen
        }
        lucuma_user_preferences_by_pk(user_id: $user_id) {
          aladin_mouse_scroll
        }
      }
    """
  }

  @GraphQL
  trait UserElevationPlotPreferencesQuery extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query plot_preferences($user_id: String! = "") {
        lucuma_user_preferences_by_pk(user_id: $user_id) {
          elevation_plot_range
          elevation_plot_time
        }
      }
    """
  }

  @GraphQL
  trait UserPreferencesAladinUpdate extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      mutation user_preferences_upsert($user_id: String = "", $aladin_mouse_scroll: Boolean = false) {
        insert_lucuma_user_preferences_one(
          object: {
            user_id: $user_id,
            aladin_mouse_scroll: $aladin_mouse_scroll
          },
          on_conflict: {
            constraint: lucuma_user_preferences_pkey,
            update_columns: aladin_mouse_scroll
          }
        ) {
          user_id
        }
      }
    """
  }

  @GraphQL
  trait UserPreferencesElevPlotUpdate extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      mutation user_preferences_upsert($user_id: String = "", $elevationPlotRange: elevation_plot_range = "", $elevationPlotTime: elevation_plot_time = "") {
        insert_lucuma_user_preferences_one(
          object: {
            user_id: $user_id,
            elevation_plot_range: $elevationPlotRange,
            elevation_plot_time: $elevationPlotTime
          },
          on_conflict: {
            constraint: lucuma_user_preferences_pkey,
            update_columns: [
              elevation_plot_range,
              elevation_plot_time
            ]
          }
        ) {
          user_id
        }
      }
    """
  }

  @GraphQL
  trait UserTargetPreferencesUpsert extends GraphQLOperation[UserPreferencesDB] {
    val document =
      """mutation target_preferences_upsert($objects: lucuma_target_insert_input! = {}) {
        insert_lucuma_target(objects: [$objects], on_conflict: {constraint: lucuma_target_pkey, update_columns: target_id}) {
          affected_rows
        }
      }"""
  }

  @GraphQL
  trait UserTargetViewOffsetUpdate extends GraphQLOperation[UserPreferencesDB] {
    val document =
      """ mutation update_target_view_offsetv($user_id: String!, $target_id: String!, $viewOffsetP: bigint!, $viewOffsetQ: bigint!) {
        update_lucuma_target_preferences_by_pk(
          pk_columns: {
            user_id: $user_id,
            target_id: $target_id
          }
          _set: {
            viewOffsetP: $viewOffsetP,
            viewOffsetQ: $viewOffsetQ
          }
        ) {
          target_id
        }
      }"""
  }

  @GraphQL
  trait UserTargetPreferencesFovUpdate extends GraphQLOperation[UserPreferencesDB] {
    val document =
      """ mutation update_target_fov($user_id: String!, $target_id: String!, $fovRA: bigint!, $fovDec: bigint!) {
        update_lucuma_target_preferences_by_pk(
          pk_columns: {
            user_id: $user_id,
            target_id: $target_id
          }
          _set: {
            fovRA: $fovRA,
            fovDec: $fovDecc
          }
        ) {
          target_id
        }
      }"""
  }
  @GraphQL
  trait ItcPlotPreferencesQuery        extends GraphQLOperation[UserPreferencesDB] {
    val document = """
      query itc_plot_preferences($user_id: String! = "", $observation_id: String! = "") {
        lucuma_itc_plot_preferences_by_pk(observation_id: $observation_id, user_id: $user_id) {
          chart_type
          details_open
        }
      }
    """
  }

  @GraphQL
  trait ItcPlotObservationUpsert extends GraphQLOperation[UserPreferencesDB] {
    val document =
      """mutation observation_preferences_upsert($objects: lucuma_observation_insert_input! = {}) {
        insert_lucuma_observation(objects: [$objects], on_conflict: {constraint: lucuma_observation_pkey, update_columns: observation_id}) {
          affected_rows
        }
      }"""
  }

}
