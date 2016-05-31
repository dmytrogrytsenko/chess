package chess

import akka.actor.Props
import chess.common.actors.BaseActor
import chess.common.Messages.Start
import chess.repositories._
import chess.rest.{ExternalRestService, InternalRestService}
import chess.settings.ChessSettings
import reactivemongo.api.MongoDriver

object ChessGuardian {
  def props(settings: ChessSettings) =
    Props(classOf[ChessGuardian], settings)
}

class ChessGuardian(settings: ChessSettings) extends BaseActor {

  var driver: MongoDriver = null

  override def preStart(): Unit = {
    super.preStart()
    driver = MongoDriver()
  }

  override def postStop(): Unit = {
    super.postStop()
    driver.close()
  }

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
    val connection = driver.connection(settings.mongo.hosts)
    val db = connection(settings.mongo.db)
    UserRepository.create(db)
    SessionRepository.create(db)
    VersionRepository.create(db)
    InvitationRepository.create(db)
    GameRepository.create(db)
  }

  def hasRole(role: String) = cluster.selfRoles.exists(r => r == role || r == "all")
}
