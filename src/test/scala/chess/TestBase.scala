package chess

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.testkit.TestKit
import akka.util.Timeout
import chess.common._
import chess.domain.{LoginResult, LoginData, User}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

abstract class TestBase
  extends TestKit(ActorSystem("test"))
  with FlatSpecLike
  with Matchers
  with EntityBuilders
  with DateTimeRangeSupport
  with RestSupport
  with MongoSupport {

  implicit val materializer: Materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.seconds)

  sys.addShutdownHook(system.terminate().await)
}


