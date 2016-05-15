package chess.rest

import chess.TestBase
import chess.common._
import chess.domain.{User, RegisterResult, RegisterData}
import chess.rest.Errors.{Conflict, BadRequest}
import org.joda.time.DateTime

import scala.concurrent.duration._

class RegisterTest extends TestBase {

  behavior of "POST /register"

  it should "create new project" in {
    //arrange
    val data = buildRegisterData()
    //act
    val result = Rest.register(data).to[RegisterResult]
    //assert
    result.user.id should not be empty
    result.user.name shouldBe data.name
    result.user.displayName shouldBe data.displayName
    result.user.createdAt shouldBeInRange DateTime.now +- 2.seconds
    val storedUser = Mongo.getUser(result.user.id)
    storedUser shouldBe Some(User(result.user.id, data.name, data.password, data.displayName, result.user.createdAt))
    //cleanup
    Mongo.removeUser(result.user.id)
  }

  it should "return 400 (Bad Request) VALIDATION if name is empty" in {
    //arrange
    val data = buildRegisterData(name = "")
    //act
    val result = Rest.register(data).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("name"))
  }

  it should "return 400 (Bad Request) VALIDATION if password is empty" in {
    //arrange
    val data = buildRegisterData(password = "")
    //act
    val result = Rest.register(data).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("password"))
  }

  it should "return 409 (Conflict) USER_ALREADY_EXISTS if user already exists" in {
    //arrange
    val user = Mongo.addUser()
    //act
    val data = buildRegisterData(name = user.name)
    val result = Rest.register(data).toErrorResult
    //assert
    result should be (Conflict.userAlreadyExists(user.name))
    //cleanup
  }
}
