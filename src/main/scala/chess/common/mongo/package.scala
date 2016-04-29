package chess.common

package object mongo {
  def isDuplicateKeyError(code: Option[Int]): Boolean = code.exists(_ == 11000)
}
