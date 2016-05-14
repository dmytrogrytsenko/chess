package chess

import akka.actor.Props
import chess.common.actors.BaseActor
import chess.common.Messages.Start
import chess.repositories.{InvitationRepository, VersionRepository, SessionRepository, UserRepository}
import chess.rest.{ExternalRestService, InternalRestService}
import chess.settings.ChessSettings
import reactivemongo.api.MongoDriver

object ChessGuardian {
  def props(settings: ChessSettings) =
    Props(classOf[ChessGuardian], settings)
}

class ChessGuardian(settings: ChessSettings) extends BaseActor {

  def receive = {
    case Start =>
      createRepositories()
      if (hasRole("rest")) {
        InternalRestService.create(settings.rest.internal)
        ExternalRestService.create(settings.rest.external)
      }
  }

  def createRepositories(): Unit = {
    import context.dispatcher
    val driver = new MongoDriver(context.system)
    val connection = driver.connection(settings.mongo.hosts)
    val db = connection.db(settings.mongo.db)
    UserRepository.create(db)
    SessionRepository.create(db)
    VersionRepository.create(db)
    InvitationRepository.create(db)
  }

  def hasRole(role: String) = cluster.selfRoles.exists(r => r == role || r == "all")
}
