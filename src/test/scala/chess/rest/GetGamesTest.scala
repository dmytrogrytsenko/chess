package chess.rest

import chess.TestBase
import chess.domain.GamesData
import chess.domain.Identifiers.Token
import chess.rest.Errors.Unauthorized

class GetGamesTest extends TestBase {

  behavior of "GET /games"

  it should "return player's games" in {
    //arrange
    val user1, user2, user3 = Mongo.addUser()
    val game1 = Mongo.addGame(whitePlayerId = user1.id, blackPlayerId = user2.id)
    val game2 = Mongo.addGame(whitePlayerId = user3.id, blackPlayerId = user1.id)
    val game3 = Mongo.addGame(whitePlayerId = user2.id, blackPlayerId = user3.id)
    val session = Mongo.addSession(userId = user1.id)
    //act
    val result = Rest.getGames(session.token).to[GamesData]
    //assert
    result shouldBe GamesData(List(game1, game2), List(user1, user2, user3))
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user1, user2, user3)
    Mongo.removeGames(game1, game2, game3)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.getGames(Token.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }
}
