package chess

import chess.common._
import chess.domain.RegisterData

trait EntityBuilders {

  def randomString = newUUID

  def buildRegisterData(name: String = randomString,
                        password: String = randomString,
                        displayName: Option[String] = Some(randomString)) =
    RegisterData(name, password, displayName)

}
