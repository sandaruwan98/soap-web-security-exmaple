ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "soap-web-security-exmaple"
  )

libraryDependencies ++= Seq(
  "org.apache.ws.security"     % "wss4j"                      % "1.6.19",
  "jakarta.xml.soap"           % "jakarta.xml.soap-api"       % "3.0.2",
  "com.sun.xml.messaging.saaj" % "saaj-impl"                  % "3.0.4"
)