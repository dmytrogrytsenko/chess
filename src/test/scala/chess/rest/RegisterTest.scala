package chess.rest

import chess.TestBase
import chess.common._
import chess.domain.{User, UserRegistrationResult, UserRegistrationData}
import org.joda.time.DateTime

import scala.concurrent.duration._

class RegisterTest extends TestBase {

  behavior of "POST /register"

  it should "create new project" in {
    //arrange
    val data = UserRegistrationData(name = newUUID, password = newUUID, displayName = Some(newUUID))
    //act
    val result = Rest.register(data).to[UserRegistrationResult]
    //assert
    result.id should not be empty
    result.name shouldBe data.name
    result.displayName shouldBe data.displayName
    result.createdAt shouldBeInRange DateTime.now +- 1.second
    val storedUser = Mongo.getUser(result.id)
    storedUser shouldBe Some(User(result.id, data.name, data.password, data.displayName, result.createdAt))
    //cleanup
    Mongo.removeUser(result.id)
  }
}
