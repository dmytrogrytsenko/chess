package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.Token
import chess.domain.Session
import chess.mongo.SessionCollection
import reactivemongo.api.DB

object SessionRepository extends NodeSingleton1[SessionRepository, DB] {
  case class AddSession(session: Session)
  case class SessionAdded(token: Token)

  case class RemoveSession(token: Token)
  case class SessionRemoved(token: Token)

  case class GetSession(token: Token)
  case class SessionFound(session: Session)
  case class SessionNotFound(token: Token)

  case class Activity(token: Token)
  case class ActivityCompleted(token: Token)
}

class SessionRepository(implicit val db: DB) extends BaseActor {

  import SessionCollection._
  import SessionRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case AddSession(session) =>
      add(session) map (_ => SessionAdded(session.token)) pipeTo sender()

    case RemoveSession(token) =>
      remove(token) map (_ => SessionRemoved(token)) pipeTo sender()

    case GetSession(token) =>
      get(token) map {
        case Some(session) => SessionFound(session)
        case None => SessionNotFound(token)
      } pipeTo sender()

    case Activity(token) =>
      activity(token) map { _ =>
        ActivityCompleted(token)
      } pipeTo sender()
  }
}
