package chess

import akka.util.Timeout
import chess.common._
import chess.domain.Identifiers._
import chess.domain.{Session, User}
import chess.mongo._
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
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

    def addUser(id: UserId = UserId.generate(),
                name: String = newUUID,
                password: String = newUUID,
                displayName: Option[String] = Some(newUUID),
                createdAt: DateTime = DateTime.now): User = {
      val user = User(id, name, password, displayName, createdAt)
      import UserCollection.UserWriter
      UserCollection.add(user)
      user
    }

    def removeUser(id: UserId) = {
      import UserCollection.UserIdWriter
      UserCollection.remove(id).await
    }

    def addSession(token: Token = newUUID.toToken,
                   userId: UserId = UserId.generate(),
                   createdAt: DateTime = DateTime.now,
                   lastActivityAt: DateTime = DateTime.now): Session = {
      val session = Session(token, userId, createdAt, lastActivityAt)
      import SessionCollection.SessionWriter
      SessionCollection.add(session).await
      session
    }

    def removeSession(token: Token) = SessionCollection.remove(token).await

    def getSession(token: Token): Option[Session] = {
      import SessionCollection.SessionReader
      SessionCollection.get(token).await
    }

  }
}
