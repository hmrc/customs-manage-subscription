import com.typesafe.sbt.packager.MappingsHelper._
import play.core.PlayVersion
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

import scala.language.postfixOps

mappings in Universal ++= directory(baseDirectory.value / "public")
// my understanding is publishing processed changed when we moved to the open and
// now it is done in production mode (was in dev previously). hence, we encounter the problem accessing "public" folder
// see https://stackoverflow.com/questions/36906106/reading-files-from-public-folder-in-play-framework-in-production

name := "customs-manage-subscription"

majorVersion := 0

targetJvm := "jvm-1.8"

scalaVersion := "2.12.10"

lazy val microservice = (project in file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(publishingSettings: _*)
  .settings(
    commonSettings
  )

lazy val commonSettings: Seq[Setting[_]] = scalaSettings ++ publishingSettings ++ defaultSettings() ++ scoverageSettings

lazy val scoverageSettings: Seq[Setting[_]] = Seq(
  coverageExcludedPackages := List("<empty>", ".*(Reverse|Routes).*", ".*Logger.*", "uk.gov.hmrc.customs.managesubscription.config.*",".*(BuildInfo|Routes).*",".*ConfigModule.*",".*ErrorResponse.*").mkString(";"),
  coverageMinimum := 95,
  coverageFailOnMinimum := true,
  coverageHighlighting := true,
  parallelExecution in Test := false
)

scalastyleConfig := baseDirectory.value / "project" / "scalastyle-config.xml"

javaOptions in Test += "-Dlogger.resource=logback-test.xml"

libraryDependencies ++= Seq(
  ws exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
  "uk.gov.hmrc" %% "bootstrap-backend-play-27" % "3.3.0",
  "uk.gov.hmrc" %% "http-caching-client" % "9.2.0-play-27",
  "uk.gov.hmrc" %% "mongo-caching" % "6.16.0-play-27",
  "uk.gov.hmrc" %% "logback-json-logger" % "4.6.0",
  "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3" % "test",
  "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % "test",
  "org.mockito" % "mockito-core" % "3.7.7" % "test",
  "uk.gov.hmrc" %% "reactivemongo-test" % "4.22.0-play-27" % Test
)

