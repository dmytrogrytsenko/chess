package chess.common

package object mongo {
  def isDuplicateKeyError(code: Option[Int]): Boolean = code.contains(11000)
}
