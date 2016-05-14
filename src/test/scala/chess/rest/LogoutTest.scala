package chess.rest

import chess.TestBase
import chess.domain.Identifiers._

class LogoutTest extends TestBase {

  behavior of "POST /logout"

  it should "logout the user" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    Rest.logout(session.token).shouldBeOK()
    //assert
    Mongo.getSession(session.token) shouldBe None
    //cleanup
    Mongo.removeUser(user.id)
  }

  it should "do nothing if token not found" in {
    //act
    Rest.logout(Token.generate()).shouldBeOK()
  }
}
