ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "blood-demand-watcher",

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %% "core" % "4.0.13",
      "org.jsoup" % "jsoup" % "1.22.1"
    )
  )
