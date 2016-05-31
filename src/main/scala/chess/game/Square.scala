package chess.game

case class File(index: Int) {
  require(File.isValid(index))
  val name = (index + 'a' - 1).toChar
  def squares = Square.byFile(this)
}

object File {
  val all = (1 to 8).map(File.apply)
  def apply(name: Char): File = File(name - 'a' + 1)
  def isValid(index: Int) = index >= 1 && index <= 8
  def get(index: Int) = if (isValid(index)) Some(File(index)) else None
}

case class Rank(index: Int) {
  require(Rank.isValid(index))
  val name = (index + '0').toChar
  def squares = Square.byRank(this)
}

object Rank {
  val all = (1 to 8).map(Rank.apply)
  def apply(name: Char): Rank = Rank(name - '0')
  def isValid(index: Int) = index >= 1 && index <= 8
  def get(index: Int) = if (isValid(index)) Some(Rank(index)) else None
}

case class Square(file: File, rank: Rank) {
  val index = file.index * 10 + rank.index
  val name = file.name.toString + rank.name.toString
  def sibling(fileDelta: Int, rankDelta: Int): Option[Square] =
    Square.get(file.index + fileDelta, rank.index + rankDelta)
}

object Square {
  val all = for {
    file <- File.all
    rank <- Rank.all
  } yield Square(file, rank)

  val byRank = all.groupBy(_.rank)
  val byFile = all.groupBy(_.file)

  def apply(name: String): Square = {
    require(name.length == 2)
    Square(File(name.head), Rank(name.last))
  }

  def get(fileIndex: Int, rankIndex: Int): Option[Square] =
    for {
      file <- File.get(fileIndex)
      rank <- Rank.get(rankIndex)
    } yield Square(file, rank)
}

