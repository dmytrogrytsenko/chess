package chess

import java.util.UUID
import java.util.concurrent.{TimeoutException, TimeUnit}

import akka.actor._
import akka.util.Timeout
import chess.common.Messages.Start
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.util.Failure

package object common {
  type Tagged[U] = { type Tag = U }
  type @@[T, U] = T with Tagged[U]

  def newUUID = UUID.randomUUID().toString

  implicit class PipedObject[T](value: T) {
    def |>[R](f: T => R) = f(this.value)
    def pipe[R](f: T => R) = |>(f)
  }

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan[DateTime](_ isBefore _)

  implicit class DateTimeOpts(self: DateTime) {
    def >(operand: DateTime): Boolean = self isAfter operand
    def <(operand: DateTime): Boolean = self isBefore operand
    def >=(operand: DateTime): Boolean = !self.isBefore(operand)
    def <=(operand: DateTime): Boolean = !self.isAfter(operand)
    def +(operand: FiniteDuration): DateTime = self plusMillis operand.toMillis.toInt
    def -(operand: FiniteDuration): DateTime = self minusMillis operand.toMillis.toInt
    def -(operand: DateTime): FiniteDuration = FiniteDuration(self.getMillis - operand.getMillis, TimeUnit.MILLISECONDS)
    def min(operand: DateTime): DateTime = if (self < operand) self else operand
    def max(operand: DateTime): DateTime = if (self > operand) self else operand
  }

  def normalizeAskResult(msg: Any): Future[Any] = msg match {
    case Failure(exception) => Future.failed(exception)
    case Status.Failure(exception) => Future.failed(exception)
    case ReceiveTimeout => Future.failed(new TimeoutException())
    case result => Future.successful(result)
  }

  implicit class RichFuture[T](val future: Future[T]) {
    def await(implicit timeout: Timeout) = Await.result(future, timeout.duration)

    def pipeTo(destination: ActorRef)(implicit executionContext: ExecutionContext) =
      future recover { case e => Status.Failure(e) } map { destination ! _ }

    def |=>(destination: ActorRef)(implicit executionContext: ExecutionContext) = pipeTo(destination)
  }

  implicit class RichProps(props: Props) {
    def create(implicit context: ActorContext) = {
      val actor = context.actorOf(props)
      actor ! Start
      actor
    }

    def create(name: String)(implicit context: ActorContext) = {
      val actor = context.actorOf(props, name)
      actor ! Start
      actor
    }
  }

}
