package chess.rest

import chess.TestBase
import chess.domain.Identifiers.Token
import chess.domain.{ProfileResult, UserData}
import chess.rest.Errors.Unauthorized

class GetProfileTest extends TestBase {

  behavior of "GET /profile"

  it should "return profile of the user" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val result = Rest.getProfile(session.token).to[ProfileResult]
    //assert
    result shouldBe ProfileResult(UserData(user))
    //cleanup
    Mongo.removeSessions(session)
    Mongo.removeUsers(user)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if token is incorrect" in {
    //act
    val result = Rest.getProfile(Token.generate()).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }
}
