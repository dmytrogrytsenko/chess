package chess.rest

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.unmarshalling.{Unmarshaller, PredefinedFromStringUnmarshallers}
import chess.common.Messages.Done
import chess.domain.Identifiers._
import chess.domain._
import chess.rest.Errors.ErrorResult
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol with PredefinedFromStringUnmarshallers {

  implicit val versionFromStringUnmarshaller = intFromStringUnmarshaller.map(_.toVersion)
  implicit val userIdFromStringUnmarshaller = Unmarshaller.strict[String, UserId](_.toUserId)
  implicit val invitationIdFromStringUnmarshaller = Unmarshaller.strict[String, InvitationId](_.toInvitationId)

  implicit object DoneJsonFormat extends RootJsonFormat[Done] {
    def write(value: Done): JsValue = JsString("")
    def read(json: JsValue): Done = Done
  }

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    private lazy val format = ISODateTimeFormat.dateTime()
    def read(json: JsValue): DateTime = json match {
      case JsString(x) => format.parseDateTime(x)
      case x => deserializationError("Expected DateTime as JsString, but got " + x)
    }
    def write(datetime: DateTime): JsValue = JsString(format.print(datetime.withZone(DateTimeZone.UTC)))
  }

  implicit object StatusCodeJsonFormat extends RootJsonFormat[StatusCode] {
    def write(statusCode: StatusCode): JsValue = JsNumber(statusCode.intValue())

    def read(json: JsValue): StatusCode = json match {
      case JsNumber(x) => StatusCode.int2StatusCode(x.toInt)
      case x => deserializationError("Expected StatusCode as JsNumber, but got " + x)
    }
  }

  implicit object VersionJsonFormat extends JsonFormat[Version] {
    def read(json: JsValue): Version = json match {
      case JsNumber(value) if value.isValidInt => value.toInt.toVersion
      case JsNumber(value) => throw new DeserializationException("Expected Version as valid integer value")
      case _ => throw new DeserializationException("Expected Version as JsNumber")
    }
    def write(value: Version): JsValue = JsNumber(value)
  }

  implicit object UserIdJsonFormat extends JsonFormat[UserId] {
    def read(json: JsValue): UserId = json match {
      case JsString(value) => value.toUserId
      case _ => throw new DeserializationException("Expected UserId as JsString")
    }
    def write(value: UserId): JsValue = JsString(value)
  }

  implicit object TokenJsonFormat extends JsonFormat[Token] {
    def read(json: JsValue): Token = json match {
      case JsString(value) => value.toToken
      case _ => throw new DeserializationException("Expected Token as JsString")
    }
    def write(value: Token): JsValue = JsString(value)
  }

  implicit object InvitationIdJsonFormat extends JsonFormat[InvitationId] {
    def read(json: JsValue): InvitationId = json match {
      case JsString(value) => value.toInvitationId
      case _ => throw new DeserializationException("Expected InvitationId as JsString")
    }
    def write(value: InvitationId): JsValue = JsString(value)
  }

  implicit object InvitationStatusJsonFormat extends JsonFormat[InvitationStatus] {
    def read(json: JsValue): InvitationStatus = json match {
      case JsString(value) => InvitationStatus.parse(value).getOrElse {
        throw new DeserializationException("Unknown InvitationStatus")
      }
      case _ => throw new DeserializationException("Expected InvitationStatus as JsString")
    }
    def write(status: InvitationStatus): JsValue = JsString(status.value)
  }

  implicit val jsonErrorResult = jsonFormat3(ErrorResult.apply)
  implicit val jsonUserData = jsonFormat4(UserData.apply)
  implicit val jsonRegisterData = jsonFormat3(RegisterData.apply)
  implicit val jsonRegisterResult = jsonFormat1(RegisterResult.apply)
  implicit val jsonLoginData = jsonFormat2(LoginData.apply)
  implicit val jsonLoginResult = jsonFormat2(LoginResult.apply)
  implicit val jsonProfileResult = jsonFormat1(ProfileResult.apply)
  implicit val jsonInvitationData = jsonFormat6(InvitationData.apply)
  implicit val jsonPlayersData = jsonFormat4(PlayersData.apply)
}
