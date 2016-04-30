package chess.rest

import chess.TestBase
import chess.common._
import chess.domain.{User, RegisterResult, RegisterData}
import chess.rest.Errors.BadRequest
import org.joda.time.DateTime

import scala.concurrent.duration._

class RegisterTest extends TestBase {

  behavior of "POST /register"

  it should "create new project" in {
    //arrange
    val data = RegisterData(name = newUUID, password = newUUID, displayName = Some(newUUID))
    //act
    val result = Rest.register(data).to[RegisterResult]
    //assert
    result.user.id should not be empty
    result.user.name shouldBe data.name
    result.user.displayName shouldBe data.displayName
    result.user.createdAt shouldBeInRange DateTime.now +- 1.second
    val storedUser = Mongo.getUser(result.user.id)
    storedUser shouldBe Some(User(result.user.id, data.name, data.password, data.displayName, result.user.createdAt))
    //cleanup
    Mongo.removeUser(result.user.id)
  }

  it should "return 400 (Bad Request) VALIDATION if name is empty" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val data = buildRegisterData(name = "")
    val result = Rest.register(data).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("name"))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }

  it should "return 400 (Bad Request) VALIDATION if password is empty" in {
    //arrange
    val user = Mongo.addUser()
    val session = Mongo.addSession(userId = user.id)
    //act
    val data = buildRegisterData(password = "")
    val result = Rest.register(data).toErrorResult
    //assert
    result should be (BadRequest.Validation.requiredMemberEmpty("password"))
    //cleanup
    Mongo.removeUser(user.id)
    Mongo.removeSession(session.token)
  }
}
