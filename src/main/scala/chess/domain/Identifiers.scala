package chess.domain

import chess.common._

object Identifiers {

  sealed trait VERSION
  sealed trait USER_ID
  sealed trait TOKEN
  sealed trait INVITATION_ID

  type Version = Int @@ VERSION
  type UserId = String @@ USER_ID
  type Token = String @@ TOKEN
  type InvitationId = String @@ INVITATION_ID

  implicit class TaggedIntegerOpts(value: Int) {
    def toVersion = value.asInstanceOf[Version]
  }

  implicit class TaggedStringOpts(value: String) {
    def toUserId = value.asInstanceOf[UserId]
    def toToken = value.asInstanceOf[Token]
    def toInvitationId = value.asInstanceOf[InvitationId]
  }

  implicit class VersionOpts(value: Int) {
    def next: Version = (value + 1).toVersion
  }

  object Version {
    val initial = 1.toVersion
  }

  object UserId {
    def generate() = newUUID.toUserId
  }

  object Token {
    def generate() = newUUID.toToken
  }

  object InvitationId {
    def generate() = newUUID.toInvitationId
  }
}
