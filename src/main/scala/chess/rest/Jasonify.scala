package chess.rest

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpCharsets._
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings.RoutingSettings
import akka.pattern.AskTimeoutException
import chess.common._
import chess.rest.Errors._
import spray.json._

import scala.collection.immutable.Seq
import scala.util.matching.Regex

trait Jasonify extends Routes {

  implicit def exceptionHandler(implicit settings: RoutingSettings): ExceptionHandler =
    ExceptionHandler {
      case e: RestException =>
        complete(e.result.status -> e.result)
      case e: AskTimeoutException =>
        complete(InternalServerError.timeout.status -> InternalServerError.timeout)
      case e if ExceptionHandler.default(settings).isDefinedAt(e) =>
        jasonifyException(e)
    }

  implicit val jsonRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handleAll[Rejection](jasonifyRejections)
      .handleNotFound(complete(NotFound.resourceNotFound))
      .result()

  private def jasonifyException(exception: Throwable)(implicit settings: RoutingSettings) =
    mapResponse(jasonifyExceptionResponse(exception)) {
      ExceptionHandler.default(settings)(exception)
    }

  private def jasonifyRejections(rejections: Seq[Rejection]): Route =
    mapResponse(jasonifyRejectionResponse(rejections)) {
      RejectionHandler.default.apply(rejections).getOrElse{
        complete(InternalServerError.default.status -> InternalServerError.default)
      }
    }

  private val wordPattern = "[A-Z][^A-Z]*".r
  private val rejectionSuffixPattern = "Rejection$".r
  private val exceptionSuffixPattern = "Exception$".r

  private def classToCode(suffixPattern: Regex)(value: String): ErrorCode = value
    .replace("$", "")
    .pipe(suffixPattern.replaceFirstIn(_, ""))
    .pipe(wordPattern.findAllMatchIn)
    .map(_.matched)
    .mkString("_")
    .toUpperCase

  private def exceptionToErrorCode(exception: Throwable): ErrorCode =
    classToCode(exceptionSuffixPattern)(exception.getClass.getSimpleName)

  private def rejectionsToErrorCode(rejections: Seq[Rejection]): ErrorCode  = rejections match {
    case AuthenticationFailedRejection(authCause, _) :: _ =>
      classToCode(rejectionSuffixPattern)(authCause.getClass.getSimpleName)
    case rejection :: _ =>
      classToCode(rejectionSuffixPattern)(rejection.getClass.getSimpleName)
    case Nil => "RESOURCE_NOT_FOUND"
  }

  private def jasonifyExceptionResponse(exception: Throwable)(response: HttpResponse): HttpResponse =
    response.mapEntity {
      case HttpEntity.Strict(ContentType(mediaType, Some(`UTF-8`)), data) if mediaType.isText =>
        val result = ErrorResult(response.status, exceptionToErrorCode(exception), data.utf8String)
        HttpEntity(`application/json`, result.toJson.prettyPrint)
    }

  private def jasonifyRejectionResponse(rejections: Seq[Rejection])
                                       (response: HttpResponse): HttpResponse =
    response.mapEntity {
      case HttpEntity.Strict(ContentType(mediaType, Some(`UTF-8`)), data) if mediaType.isText =>
        val result = ErrorResult(response.status, rejectionsToErrorCode(rejections), data.utf8String)
        HttpEntity(`application/json`, result.toJson.prettyPrint)
      case res => res
    }
}
