package chess.rest

import chess.TestBase
import chess.domain.{UserData, LoginResult, LoginData}
import chess.rest.Errors.Unauthorized

class LoginTest extends TestBase {

  behavior of "POST /login"

  it should "login the user" in {
    //arrange
    val user = Mongo.addUser()
    val data = LoginData(user.name, user.password)
    //act
    val result = Rest.login(data).to[LoginResult]
    //assert
    result.token should not be empty
    result.user shouldBe UserData(user)
    //cleanup
    Mongo.removeSession(result.token)
    Mongo.removeUser(user.id)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if user not found" in {
    //arrange
    val data = buildLoginData()
    //act
    val result = Rest.login(data).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
  }

  it should "return 401 (Unauthorized) CREDENTIALS_REJECTED if password is incorrect" in {
    //arrange
    val user = Mongo.addUser()
    val data = buildLoginData(name = user.name)
    //act
    val result = Rest.login(data).toErrorResult
    //assert
    result should be (Unauthorized.credentialsRejected)
    //cleanup
    Mongo.removeUser(user.id)
  }
}
