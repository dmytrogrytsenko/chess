package chess

import java.util.UUID

import akka.actor.{Status, ActorRef}
import akka.util.Timeout

import scala.concurrent.{Future, ExecutionContext, Await}

package object common {
  def newUUID = UUID.randomUUID().toString

  implicit class PipedObject[T](value: T) {
    def |>[R] (f: T => R) = f(this.value)
    def pipe[R](f: T => R) = |>(f)
  }

  implicit class RichFuture[T](val future: Future[T]) {
    def await(implicit timeout: Timeout) = Await.result(future, timeout.duration)

    def pipeTo(destination: ActorRef)
              (implicit executionContext: ExecutionContext) =
      future recover { case e => Status.Failure(e) } map { destination ! _ }

    def |=>(destination: ActorRef)(implicit executionContext: ExecutionContext) = pipeTo(destination)
  }

}
