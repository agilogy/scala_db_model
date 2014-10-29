import bintray.Keys._
import com.typesafe.sbt.SbtGit._

name := "scala_db_model"

organization := "com.agilogy"

version := "0.0.1"

publishMavenStyle := false

bintraySettings

repository in bintray := "scala-libs"

bintrayOrganization in bintray := None

packageLabels in bintray := Seq("scala")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

versionWithGit

scalaVersion := "2.11.2"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1102-jdbc41" % "test"

