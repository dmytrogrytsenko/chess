package chess.rest

import akka.http.scaladsl.model.StatusCode
import chess.domain.Identifiers._
import chess.domain.{UserRegistrationResult, UserRegistrationData}
import chess.rest.Errors.ErrorResult
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import spray.json._

trait JsonProtocol extends DefaultJsonProtocol {
  //val UserIdSegment: PathMatcher1[UserId] = Segment.map(_.toUserId)

  //implicit val UserIdDeserializer = new FromStringDeserializer[UserId] {
  //  def apply(value: String): Either[DeserializationError, UserId] = Right(value.toUserId)
  //}

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    private lazy val format = ISODateTimeFormat.dateTimeNoMillis()
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

  implicit object UserIdJsonFormat extends JsonFormat[UserId] {
    def read(json: JsValue): UserId = json match {
      case JsString(value) => value.toUserId
      case _ => throw new DeserializationException("Expected ProjectId as JsString")
    }
    def write(value: UserId): JsValue = JsString(value)
  }

  implicit val jsonErrorResult = jsonFormat3(ErrorResult.apply)
  implicit val jsonUserRegistrationData = jsonFormat3(UserRegistrationData.apply)
  implicit val jsonUserRegistrationResult = jsonFormat4(UserRegistrationResult.apply)

}
