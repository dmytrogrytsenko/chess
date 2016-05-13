package chess.mongo

import chess.TestBase
import chess.common._
import chess.domain.Identifiers._

import scala.concurrent.ExecutionContext.Implicits.global

class VersionCollectionTest extends TestBase {

  import VersionCollection._

  behavior of "VersionReader and VersionWriter"

  it should "read and write version correctly" in {
    //arrange
    val version = Version.initial.next
    //act
    val result = VersionReader.read(VersionWriter.write(version))
    //assert
    result shouldBe version
  }

  behavior of "VersionItemReader and VersionItemWriter"

  it should "read and write VersionItem correctly" in {
    //arrange
    val item = buildVersionItem()
    //act
    val result = VersionItemReader.read(VersionItemWriter.write(item))
    //assert
    result shouldBe item
  }

  behavior of "getVersion"

  it should "return initial version if version not found" in {
    //act
    val result = getVersion(randomString).await
    //assert
    result shouldBe Version.initial
  }

  it should "return version" in {
    //arrange
    val item = Mongo.addVersionItem()
    //act
    val result = getVersion(item.name).await
    //assert
    result shouldBe item
    //cleanup
    Mongo.removeVersionItem(item.name)
  }

}
