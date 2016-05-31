package chess.rest

import chess.TestBase
import chess.domain.Identifiers._
import chess.game.{Game, Regular, Square}
import chess.game.Pieces._
import chess.rest.Errors.{BadRequest, Forbidden, NotFound, Unauthorized}

class MoveTest extends TestBase {

  val e2 = Square("e2")
  val e4 = Square("e4")

  behavior of "GET /games/:id/move"

  it should "make movement" in {
    //arrange
    val user1, user2 = Mongo.addUser()
    val game = Mongo.addGame(Game.create(user1.id, user2.id))
    val session = Mongo.addSession(userId = user1.id)
    //act
    Rest.move(session.token, game.id, e2, e4).shouldBeOK()
    //assert
    val stored = Mongo.getGame(game.id).get
    stored.id shouldBe game.id
    stored.version shouldBe game.version.next
    stored.whitePlayerId shouldBe game.whitePlayerId
    stored.blackPlayerId shouldBe game.blackPlayerId
    stored.startTime shouldBe game.startTime
    stored.board.squares shouldBe game.board.squares - e2 + (e4 -> WhitePawn)
    stored.movingPlayer shouldBe game.movingPlayer.opposite
    stored.initials shouldBe game.initials - e2
    stored.history.size shouldBe 1
    val movement = stored.history.head
    movement.kind shouldBe Regular
    movement.piece shouldBe WhitePawn
    movement.src shouldBe e2
    movement.dst shouldBe e4
    movement.captured shouldBe None
    movement.promoted shouldBe None
    movement.castlingKind shouldBe None
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeGames(game)
    Mongo.removeUsers(user1, user2)
  }

  it should "return 400 (BadRequest) INVALID_MOVEMENT player try to make invalid movement" in {
    //assert
    val user1, user2 = Mongo.addUser()
    val game = Mongo.addGame(Game.create(user1.id, user2.id))
    val session = Mongo.addSession(userId = user1.id)
    //act
    val result = Rest.move(session.token, game.id, e2, e2).toErrorResult
    //assert
    result should be (BadRequest.invalidMovement)
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeGames(game)
    Mongo.removeUsers(user1, user2)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.move(Token.generate(), GameId.generate(), e2, e4).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 403 (Forbidden) ACCESS_DENIED if non-player try to make movement" in {
    //assert
    val user, user1, user2 = Mongo.addUser()
    val game = Mongo.addGame(Game.create(user1.id, user2.id))
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.move(session.token, game.id, e2, e4).toErrorResult
    //assert
    result should be (Forbidden.accessDenied)
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeGames(game)
    Mongo.removeUsers(user, user1, user2)
  }

  it should "return 404 (Not Found) GAME_NOT_FOUND if game not found" in {
    //assert
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.move(session.token, GameId.generate(), e2, e4).toErrorResult
    //assert
    result should be (NotFound.gameNotFound)
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user)
  }

}
