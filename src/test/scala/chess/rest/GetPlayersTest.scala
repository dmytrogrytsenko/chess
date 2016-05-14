package chess.rest

import akka.http.scaladsl.model.StatusCodes
import chess.TestBase
import chess.common._
import chess.domain.Identifiers._
import chess.domain.InvitationStatuses.Pending
import chess.domain.{UserData, PlayerData, PlayersData}
import chess.rest.Errors.Unauthorized
import org.joda.time.DateTime

import scala.concurrent.duration._

class GetPlayersTest extends TestBase {

  behavior of "GET /players"

  it should "not return player who makes request" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getPlayers(session.token).to[PlayersData]
    //assert
    result.players.exists(_.user.id == user.id) shouldBe false
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user)
  }

  it should "return online players if version not defined" in {
    //arrange
    val user, user2, user3 = Mongo.addUser()
    val Seq(session, session2) = Mongo.addSessions(user, user2)
    val session3 = Mongo.addSession(userId = user3.id, lastActivityAt = DateTime.now - 5.days)
    //act
    val result = Rest.getPlayers(session.token).to[PlayersData]
    //assert
    val expectedPlayerData = PlayerData(UserData(user2), userInvitesMe = false, userIsInvitedByMe = false)
    result.players.find(_.user.id == user2.id) shouldBe Some(expectedPlayerData)
    result.players.find(_.user.id == user3.id) shouldBe None
    //cleanup
    Mongo.removeSessions(session, session2, session3)
    Mongo.removeUsers(user, user2, user3)
  }

  it should "return online players if version changed" in {
    //arrange
    val user, user2, user3 = Mongo.addUser()
    val Seq(session, session2) = Mongo.addSessions(user, user2)
    val session3 = Mongo.addSession(userId = user3.id, lastActivityAt = DateTime.now - 5.days)
    //act
    val previousVersion = (Mongo.getPlayersVersion - 1).toVersion
    val result = Rest.getPlayers(session.token, Some(previousVersion)).to[PlayersData]
    //assert
    val expectedPlayerData = PlayerData(UserData(user2), userInvitesMe = false, userIsInvitedByMe = false)
    result.players.find(_.user.id == user2.id) shouldBe Some(expectedPlayerData)
    result.players.find(_.user.id == user3.id) shouldBe None
    //cleanup
    Mongo.removeSessions(session, session2, session3)
    Mongo.removeUsers(user, user2, user3)
  }

  it should "return 204 (No Content) if version not changed" in {
    //arrange
    val user, user2 = Mongo.addUser()
    val Seq(session, session2) = Mongo.addSessions(user, user2)
    //act
    val response = Rest.getPlayers(session.token, Some(Mongo.getPlayersVersion))
    //assert
    response.status shouldBe StatusCodes.NoContent
    response.body shouldBe ""
    //cleanup
    Mongo.removeSessions(session, session2)
    Mongo.removeUsers(user, user2)
  }

  it should "return userInvitesMe = true if other user has invited current user" in {
    //arrange
    val user, user2 = Mongo.addUser()
    val Seq(session, session2) = Mongo.addSessions(user, user2)
    val invitation = Mongo.addInvitation(inviterId = user2.id, inviteeId = user.id, status = Pending)
    //act
    val result = Rest.getPlayers(session.token).to[PlayersData]
    //assert
    val expectedPlayerData = PlayerData(UserData(user2), userInvitesMe = true, userIsInvitedByMe = false)
    result.players.find(_.user.id == user2.id) shouldBe Some(expectedPlayerData)
    //cleanup
    Mongo.removeSessions(session, session2)
    Mongo.removeUsers(user, user2)
    Mongo.removeInvitations(invitation)
  }

  it should "return userIsInvitedByMe = true if current user has invited other user" in {
    //arrange
    val user, user2 = Mongo.addUser()
    val Seq(session, session2) = Mongo.addSessions(user, user2)
    val invitation = Mongo.addInvitation(inviterId = user.id, inviteeId = user2.id, status = Pending)
    //act
    val result = Rest.getPlayers(session.token).to[PlayersData]
    //assert
    val expectedPlayerData = PlayerData(UserData(user2), userInvitesMe = false, userIsInvitedByMe = true)
    result.players.find(_.user.id == user2.id) shouldBe Some(expectedPlayerData)
    //cleanup
    Mongo.removeSessions(session, session2)
    Mongo.removeUsers(user, user2)
    Mongo.removeInvitations(invitation)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.getPlayers(Token.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }
}
