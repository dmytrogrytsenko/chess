package chess

import java.util.UUID

import chess.domain._
import chess.domain.Identifiers._
import org.joda.time.DateTime

import scala.util.Random

trait EntityBuilders {

  lazy val random = new Random(DateTime.now.getMillisOfSecond)

  def randomString = UUID.randomUUID().toString
  def randomInt = random.nextInt()
  def randomInt(n: Int) = random.nextInt(n)

  implicit class RandomSet[T](set: Set[T]) {
    def randomValue = set.toIndexedSeq(randomInt(set.size))
  }
  def buildVersionItem(name: String = randomString,
                       version: Version = Version.initial.next.next) =
    VersionItem(name, version)

  def buildRegisterData(name: String = randomString,
                        password: String = randomString,
                        displayName: Option[String] = Some(randomString)) =
    RegisterData(name, password, displayName)

  def buildLoginData(name: String = randomString,
                     password: String = randomString) =
    LoginData(name, password)

  def buildUser(id: UserId = UserId.generate(),
                name: String = randomString,
                password: String = randomString,
                displayName: Option[String] = Some(randomString),
                createdAt: DateTime = DateTime.now) =
    User(id, name, password, displayName, createdAt)

  def buildSession(token: Token = Token.generate(),
                   userId: UserId = UserId.generate(),
                   createdAt: DateTime = DateTime.now,
                   lastActivityAt: DateTime = DateTime.now) =
    Session(token, userId, createdAt, lastActivityAt)

  def buildInvitation(id: InvitationId = InvitationId.generate(),
                      inviterId: UserId = UserId.generate(),
                      inviteeId: UserId = UserId.generate(),
                      createdAt: DateTime = DateTime.now,
                      status: InvitationStatus = InvitationStatus.all.randomValue,
                      completedAt: Option[DateTime] = Some(DateTime.now)) =
    Invitation(id, inviterId, inviteeId, createdAt, status, completedAt)
}
