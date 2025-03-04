// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.model

import cats.*
import cats.effect.*
import cats.syntax.all.*
import clue.*
import clue.js.*
import clue.websocket.*
import io.circe.Json
import lucuma.schemas.*
import org.http4s.Uri.Authority
import org.http4s.Uri.Scheme
import org.http4s.*
import org.http4s.syntax.all.*
import org.typelevel.log4cats.Logger
import queries.schemas.*

case class GraphQLClients[F[_]: Async: Parallel] protected (
  odb:           WebSocketJSClient[F, ObservationDB],
  preferencesDB: WebSocketJSClient[F, UserPreferencesDB],
  itc:           FetchJSClient[F, ITC],
  sso:           FetchJSClient[F, SSO]
):
  def init(payload: Map[String, Json]): F[Unit] =
    (
      preferencesDB.connect() >> preferencesDB.initialize(),
      odb.connect() >> odb.initialize(payload),
    ).parTupled.void

  def close(): F[Unit] =
    List(
      preferencesDB.terminate() >> preferencesDB.disconnect(WebSocketCloseParams(code = 1000)),
      odb.terminate() >> odb.disconnect(WebSocketCloseParams(code = 1000))
    ).sequence.void

object GraphQLClients:
  def build[F[_]: Async: FetchJSBackend: WebSocketJSBackend: Parallel: Logger](
    odbURI:               Uri,
    prefsURI:             Uri,
    itcURI:               Uri,
    ssoURI:               Uri,
    reconnectionStrategy: WebSocketReconnectionStrategy
  ): F[GraphQLClients[F]] =
    for {
      odbClient   <-
        WebSocketJSClient.of[F, ObservationDB](odbURI.toString, "ODB", reconnectionStrategy)
      prefsClient <-
        WebSocketJSClient.of[F, UserPreferencesDB](prefsURI.toString, "PREFS", reconnectionStrategy)
      itcClient   <-
        FetchJSClient.of[F, ITC](itcURI.toString, "ITC")
      ssoClient   <-
        FetchJSClient.of[F, SSO](s"${ssoURI.toString}/graphql", "SSO")
    } yield GraphQLClients(odbClient, prefsClient, itcClient, ssoClient)
