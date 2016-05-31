package chess.rest

import akka.http.scaladsl.model.StatusCodes
import chess.TestBase
import chess.domain.GameData
import chess.domain.Identifiers._
import chess.rest.Errors.{NotFound, Unauthorized}

class GetGameTest extends TestBase {

  behavior of "GET /games/:id"

  it should "return game if version not defined" in {
    //arrange
    val user, white, black = Mongo.addUser()
    val game = Mongo.addGame(whitePlayerId = white.id, blackPlayerId = black.id)
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getGame(session.token, game.id).to[GameData]
    //assert
    result shouldBe GameData(game, List(white, black))
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user, white, black)
    Mongo.removeGames(game)
  }

  it should "return game if version changed" in {
    //arrange
    val user, white, black = Mongo.addUser()
    val game = Mongo.addGame(whitePlayerId = white.id, blackPlayerId = black.id)
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getGame(session.token, game.id, Some(game.version.next)).to[GameData]
    //assert
    result shouldBe GameData(game, List(white, black))
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user, white, black)
    Mongo.removeGames(game)
  }

  it should "return 204 (No Content) if version not changed" in {
    //arrange
    val user, white, black = Mongo.addUser()
    val game = Mongo.addGame(whitePlayerId = white.id, blackPlayerId = black.id)
    val session = Mongo.addSession(userId = user.id)
    //act
    val response = Rest.getGame(session.token, game.id, Some(game.version))
    //assert
    response.status shouldBe StatusCodes.NoContent
    response.body shouldBe ""
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user, white, black)
    Mongo.removeGames(game)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.getGame(Token.generate(), GameId.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 404 (Not Found) GAME_NOT_FOUND if game not found" in {
    //assert
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getGame(session.token, GameId.generate()).toErrorResult
    //assert
    result should be (NotFound.gameNotFound)
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user)
  }

}
