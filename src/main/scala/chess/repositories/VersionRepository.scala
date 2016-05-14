package chess.repositories

import chess.common._
import chess.common.Messages.Start
import chess.common.actors.{BaseActor, NodeSingleton1}
import chess.domain.Identifiers.Version
import chess.mongo.VersionCollection
import reactivemongo.api.DB

object VersionRepository extends NodeSingleton1[VersionRepository, DB] {
  case class GetVersion(name: String)
  case class RetrievedVersion(name: String, version: Version)
}

class VersionRepository(implicit val db: DB) extends BaseActor {

  import VersionCollection._
  import VersionRepository._
  import context.dispatcher

  def receive = {
    case Start => ensureIndexes

    case GetVersion(name) =>
      getVersion(name) map { version => RetrievedVersion(name, version) } pipeTo sender()
  }
}