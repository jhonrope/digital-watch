name := "digital-watch"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.9",
  "io.spray" %% "spray-can" % "1.3.1",
  "io.spray" %% "spray-routing" % "1.3.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "io.spray" %% "spray-testkit" % "1.3.1" % "test",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.0.0"
)
    