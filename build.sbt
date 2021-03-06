name := "chess"
 
version := "1.0-SNAPSHOT"
 
organization := "com.delirium"
 
scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Spray Repository" at "http://repo.spray.io"
)

libraryDependencies ++= {
  val akkaVersion = "2.4.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.5",
    "com.typesafe" % "config" % "1.3.0",
    "org.joda" % "joda-convert" % "1.6",
    "joda-time" % "joda-time" % "2.6",
//    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
//    "org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.10.5.0.0.akka23",
    "org.reactivemongo" %% "reactivemongo" % "0.11.11",
    "org.reactivemongo" %% "reactivemongo-extensions-bson" % "0.10.5.0.0.akka23",
    "org.scalatest" %% "scalatest" % "2.2.6",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )
}
