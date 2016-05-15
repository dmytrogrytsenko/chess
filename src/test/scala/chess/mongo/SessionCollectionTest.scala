package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers.Token
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class SessionCollectionTest extends TestBase {

  import SessionCollection._

  behavior of "TokenReader and TokenWriter"

  it should "read and write token correctly" in {
    //arrange
    val token = Token.generate()
    //act
    val result = TokenReader.read(TokenWriter.write(token))
    //assert
    result shouldBe token
  }

  behavior of "SessionReader and SessionWriter"

  it should "read and write session correctly" in {
    //arrange
    val session = buildSession()
    //act
    val result = SessionReader.read(SessionWriter.write(session))
    //assert
    result shouldBe session
  }

  behavior of "activity"

  it should "update lastActivityAt correctly" in {
    //arrange
    val session = Mongo.addSession(lastActivityAt = DateTime.now - 1.minute)
    //act
    activity(session.token).await
    //assert
    Mongo.getSession(session.token).get.lastActivityAt shouldBeInRange DateTime.now +- 2.seconds
    //cleanup
    Mongo.removeSession(session.token)
  }

  it should "do nothing if session not found" in {
    //arrange
    val token = Token.generate()
    //act
    activity(token).await
    //assert
    Mongo.getSession(token) shouldBe None
  }

  behavior of "getOnlineSessions"

  it should "return online sessions" in {
    //arrange
    val session1, session2 = Mongo.addSession()
    val session3 = Mongo.addSession(lastActivityAt = DateTime.now - 5.minutes)
    //act
    val result = getOnlineSessions(1.minute).await
    //assert
    result should contain (session1)
    result should contain (session2)
    result should not contain (session3)
    //cleanup
    Mongo.removeSessions(session1, session2, session3)
  }
}
