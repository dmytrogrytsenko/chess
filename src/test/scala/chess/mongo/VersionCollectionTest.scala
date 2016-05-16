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
    val version = randomVersion
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
    result shouldBe item.version
    //cleanup
    Mongo.removeVersionItem(item.name)
  }

  behavior of "increment"

  it should "return initial version if version not found" in {
    //arrange
    val name = randomString
    //act
    val result = increment(name).await
    //assert
    result shouldBe Version.initial
    //cleanup
    Mongo.removeVersionItem(name)
  }

  it should "increment version" in {
    //arrange
    val item = Mongo.addVersionItem()
    //act
    val result = increment(item.name).await
    //assert
    result shouldBe item.version.next
    //cleanup
    Mongo.removeVersionItem(item.name)
  }

}
