package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers.UserId

import scala.concurrent.ExecutionContext.Implicits.global

class UserCollectionTest extends TestBase {

  import UserCollection._

  behavior of "UserIdReader and UserIdWriter"

  it should "read and write UserId correctly" in {
    //arrange
    val userId = UserId.generate()
    //act
    val result = UserIdReader.read(UserIdWriter.write(userId))
    //assert
    result shouldBe userId
  }

  behavior of "UserReader and UserWriter"

  it should "read and write user correctly" in {
    //arrange
    val user = buildUser()
    //act
    val result = UserReader.read(UserWriter.write(user))
    //assert
    result shouldBe user
  }

  behavior of "findUserByName"

  it should "find user by name" in {
    //arrange
    val user = Mongo.addUser()
    //act
    val result = findUserByName(user.name).await
    //assert
    result shouldBe Some(user)
    //cleanup
    remove(user.id).await
  }

  it should "find user by name and ignore case" in {
    //arrange
    val user = Mongo.addUser(name = randomString + "ABC")
    //act
    val result = findUserByName(user.name.toLowerCase).await
    //assert
    result shouldBe Some(user)
    //cleanup
    remove(user.id).await
  }

  it should "return None if user not found" in {
    //act
    val result = findUserByName(randomString).await
    //assert
    result shouldBe None
  }

}
