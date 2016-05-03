package chess.rest

import chess.TestBase
import chess.domain.Identifiers._

class LogoutTest extends TestBase {

  behavior of "POST /logout"

  it should "logout the user" in {
    //arrange
    val user = Mongo.addUser()
    val token = loginUser(user)
    //act
    Rest.logout(token).shouldBeOK()
    //assert
    Mongo.getSession(token) shouldBe None
    //cleanup
    Mongo.removeUser(user.id)
  }

  it should "do nothing if token not found" in {
    //act
    Rest.logout(randomToken).shouldBeOK()
  }
}
