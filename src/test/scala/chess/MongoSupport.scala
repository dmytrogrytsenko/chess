package chess

import akka.util.Timeout
import chess.common._
import chess.domain.Identifiers._
import chess.domain.User
import chess.mongo._
import com.typesafe.config.ConfigFactory
import reactivemongo.api.{MongoDriver, DB}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

object MongoSupport {
  lazy val config = ConfigFactory.load()
  lazy val driver = new MongoDriver
  lazy val hosts = config.getStringList("chess.mongo.hosts").toList
  lazy val connection = driver.connection(hosts)
  lazy val db = connection("chess")
}

trait MongoSupport {

  implicit val timeout: Timeout
  implicit val db: DB = MongoSupport.db

  object Mongo {
    def getUser(id: UserId): Option[User] = {
      import UserCollection.UserIdWriter
      import UserCollection.UserReader
      UserCollection.get(id).await
    }

    def removeUser(id: UserId) = {
      import UserCollection.UserIdWriter
      UserCollection.remove(id).await
    }
  }
}
