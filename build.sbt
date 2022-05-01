ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"

ThisBuild / scalacOptions ++= Seq(
  "-deprecation"
)

lazy val root = (project in file("."))
  .settings(
    name := "dott-code-test",
    libraryDependencies ++= Seq(
      "org.mapdb" % "mapdb" % "3.0.8",
      "io.spray" %% "spray-json" % "1.3.6"
    )
  )
