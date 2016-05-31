package chess

import akka.util.Timeout
import chess.common._
import chess.domain.Identifiers._
import chess.domain._
import chess.game._
import chess.mongo._
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import reactivemongo.api.{DB, MongoDriver}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

object MongoSupport {
  val config = ConfigFactory.load()
  val driver = new MongoDriver()
  val hosts = config.getStringList("chess.mongo.hosts").toList
  val connection = driver.connection(hosts)
  val db = connection("chess")
  Thread.sleep(1000)
}

trait MongoSupport extends EntityBuilders {

  implicit val timeout: Timeout
  implicit val db: DB = MongoSupport.db

  object Mongo {
    def getVersionItem(name: String): Option[VersionItem] = {
      import VersionCollection.VersionItemReader
      VersionCollection.get(name).await
    }

    def addVersionItem(name: String = randomString,
                       version: Version = randomVersion): VersionItem = {
      val item = VersionItem(name, version)
      import VersionCollection.VersionItemWriter
      VersionCollection.add(item).await
      item
    }

    def removeVersionItem(name: String) = {
      VersionCollection.remove(name).await
    }

    def getPlayersVersion = {
      import VersionCollection._
      getVersion(players).await
    }

    def getUser(id: UserId): Option[User] = {
      import UserCollection.UserIdWriter
      import UserCollection.UserReader
      UserCollection.get(id).await
    }

    def addUser(id: UserId = UserId.generate(),
                name: String = randomString,
                password: String = randomString,
                displayName: Option[String] = Some(randomString),
                createdAt: DateTime = DateTime.now): User = {
      val user = User(id, name, password, displayName, createdAt)
      import UserCollection.UserWriter
      UserCollection.add(user).await
      user
    }

    def removeUser(id: UserId) = {
      import UserCollection.UserIdWriter
      UserCollection.remove(id).await
    }

    def removeUsers(users: User*) = {
      users.foreach(user => removeUser(user.id))
    }

    def addSession(token: Token = randomString.toToken,
                   userId: UserId = UserId.generate(),
                   createdAt: DateTime = DateTime.now,
                   lastActivityAt: DateTime = DateTime.now): Session = {
      val session = Session(token, userId, createdAt, lastActivityAt)
      import SessionCollection.SessionWriter
      SessionCollection.add(session).await
      session
    }

    def addSessions(users: User*) = {
      users.map(user => addSession(userId = user.id))
    }

    def removeSession(token: Token) = {
      import SessionCollection.TokenWriter
      SessionCollection.remove(token).await
    }

    def removeSessions(sessions: Session*) = {
      sessions.foreach(session => removeSession(session.token))
    }

    def getSession(token: Token): Option[Session] = {
      import SessionCollection.TokenWriter
      import SessionCollection.SessionReader
      SessionCollection.get(token).await
    }

    def getInvitation(id: InvitationId): Option[Invitation] = {
      import InvitationCollection.InvitationIdWriter
      import InvitationCollection.InvitationReader
      InvitationCollection.get(id).await
    }

    def addInvitation(id: InvitationId = InvitationId.generate(),
                      inviterId: UserId = UserId.generate(),
                      inviteeId: UserId = UserId.generate(),
                      createdAt: DateTime = DateTime.now,
                      status: InvitationStatus = InvitationStatus.all.randomValue,
                      completedAt: Option[DateTime] = Some(DateTime.now),
                      gameId: Option[GameId] = Some(GameId.generate())): Invitation = {
      val invitation = Invitation(id, inviterId, inviteeId, createdAt, status, completedAt, gameId)
      import InvitationCollection.InvitationWriter
      InvitationCollection.add(invitation).await
      invitation
    }

    def removeInvitation(id: InvitationId) = {
      import InvitationCollection.InvitationIdWriter
      InvitationCollection.remove(id).await
    }

    def removeInvitations(invitations: Invitation*) = {
      invitations.foreach(invitation => removeInvitation(invitation.id))
    }

    def getGame(id: GameId): Option[Game] = {
      import GameCollection.GameIdWriter
      import GameCollection.GameReader
      GameCollection.get(id).await
    }

    def addGame(id: GameId = GameId.generate(),
                version: Version = Version.initial.next,
                whitePlayerId: UserId = UserId.generate(),
                blackPlayerId: UserId = UserId.generate(),
                startTime: DateTime = DateTime.now,
                board: Board = Board.initial,
                movingPlayer: PieceColor = PieceColor.all.randomValue,
                initials: Set[Square] = Set(Square.all.toSet.randomValue, Square.all.toSet.randomValue),
                history: List[Movement] = List(buildMovement(), buildMovement())): Game = {
      val game = Game(id, version, whitePlayerId, blackPlayerId, startTime, board, movingPlayer, initials, history)
      import GameCollection.GameWriter
      GameCollection.add(game).await
      game
    }

    def removeGame(id: GameId) = {
      import GameCollection.GameIdWriter
      GameCollection.remove(id).await
    }

    def removeGames(games: Game*) = {
      games.foreach(game => removeGame(game.id))
    }

  }
}
