package chess.rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import chess.common.actors.BaseActor

import scala.concurrent.ExecutionContextExecutor

trait Routes extends BaseActor with Directives with JsonProtocol with SprayJsonSupport {
  implicit def executor: ExecutionContextExecutor
  implicit def timeout: Timeout
}