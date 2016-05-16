package chess.domain

import chess.domain.Identifiers._

case class PlayersData(onlinePlayers: List[UserData],
                       inviters: List[InvitationData],
                       invitees: List[InvitationData],
                       version: Version)

