package chess

import java.util.UUID

import chess.domain._
import chess.domain.Identifiers._
import chess.game._
import org.joda.time.DateTime

import scala.util.Random

trait EntityBuilders {

  lazy val random = new Random(DateTime.now.getMillisOfSecond)

  def randomString = UUID.randomUUID().toString
  def randomInt = random.nextInt()
  def randomInt(n: Int) = random.nextInt(n)
  def randomVersion = random.nextInt(1024).toVersion

  implicit class RandomSet[T](set: Set[T]) {
    def randomValue = set.toIndexedSeq(randomInt(set.size))
  }
  def buildVersionItem(name: String = randomString,
                       version: Version = randomVersion) =
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
                      completedAt: Option[DateTime] = Some(DateTime.now),
                      gameId: Option[GameId] = Some(GameId.generate())) =
    Invitation(id, inviterId, inviteeId, createdAt, status, completedAt, gameId)

  def buildPiece(color: PieceColor = PieceColor.all.randomValue,
                 kind: PieceKind = PieceKind.all.randomValue): Piece =
    Piece(color, kind)

  def buildMovement(kind: MovementKind = MovementKind.all.randomValue,
                    piece: Piece = buildPiece(),
                    src: Square = Square.all.toSet.randomValue,
                    dst: Square = Square.all.toSet.randomValue,
                    captured: Option[Piece] = Some(buildPiece()),
                    promoted: Option[Piece] = Some(buildPiece()),
                    castlingKind: Option[CastlingKind] = Some(CastlingKind.all.randomValue)): Movement =
    Movement(kind, piece, src, dst, captured, promoted, castlingKind)
}
