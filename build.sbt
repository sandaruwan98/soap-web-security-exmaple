ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "soap-web-security-exmaple"
  )

libraryDependencies ++= Seq(
  "org.apache.wss4j" % "wss4j-ws-security-dom" % "2.4.1",
  "org.apache.santuario" % "xmlsec" % "2.3.0"
)