package chess.domain

import chess.domain.Identifiers._

case class PlayerData(user: UserData, userInvitesMe: Boolean, userIsInvitedByMe: Boolean)

case class PlayersData(players: List[PlayerData], version: Version)

