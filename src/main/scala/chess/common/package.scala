package chess

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{Status, ActorRef}
import akka.util.Timeout
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, ExecutionContext, Await}

package object common {
  def newUUID = UUID.randomUUID().toString

  implicit class PipedObject[T](value: T) {
    def |>[R](f: T => R) = f(this.value)
    def pipe[R](f: T => R) = |>(f)
  }

  implicit class SomedObject[T](value: T) {
    def some: Option[T] = Some(value)
  }

  implicit class RichFuture[T](val future: Future[T]) {
    def await(implicit timeout: Timeout) = Await.result(future, timeout.duration)

    def pipeTo(destination: ActorRef)(implicit executionContext: ExecutionContext) =
      future recover { case e => Status.Failure(e) } map { destination ! _ }

    def |=>(destination: ActorRef)(implicit executionContext: ExecutionContext) = pipeTo(destination)
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

}
