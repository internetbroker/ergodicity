import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import net.virtualvoid.sbt.graph.Plugin._

object ErgodicityBuild extends Build {

  lazy val buildSettings = Seq(
    organization := "com.ergodicity",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.9.1"
  )

  lazy val ergodicity = Project(
    id = "ergodicity",
    base = file("."),
    aggregate = Seq(capture, engine, core, plaza2)
  ).configs( IntegrationTest )
    .settings( (Defaults.itSettings ++ graphSettings) : _*)

  lazy val capture = Project(
    id = "capture",
    base = file("capture"),
    dependencies = Seq(core),
    settings = Project.defaultSettings ++ repositoriesSetting ++ graphSettings ++ Seq(libraryDependencies ++= Dependencies.capture) ++
      assemblySettings ++ extAssemblySettings
  ).configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)

  lazy val quant = Project(
    id = "quant",
    base = file("quant"),
    dependencies = Seq(core),
    settings = Project.defaultSettings ++ repositoriesSetting ++ graphSettings ++ Seq(libraryDependencies ++= Dependencies.quant)
  ).configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)

  lazy val engine = Project(
    id = "engine",
    base = file("engine"),
    dependencies = Seq(core),
    settings = Project.defaultSettings ++ repositoriesSetting ++ graphSettings ++ Seq(libraryDependencies ++= Dependencies.engine)
  ).configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)

  lazy val core = Project(
    id = "core",
    base = file("core"),
    dependencies = Seq(plaza2),
    settings = Project.defaultSettings ++ repositoriesSetting ++ graphSettings ++ Seq(libraryDependencies ++= Dependencies.core)
  ).configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)

  lazy val plaza2 = Project(
    id = "plaza2",
    base = file("plaza2"),
    settings = Project.defaultSettings ++ repositoriesSetting ++ graphSettings ++ Seq(libraryDependencies ++= Dependencies.plaza2)
  ).configs( IntegrationTest )
    .settings( Defaults.itSettings : _*)

  // -- Settings
  
  override lazy val settings = super.settings ++ buildSettings

  lazy val repositoriesSetting = Seq(
    resolvers += "Scala tools releases" at "http://scala-tools.org/repo-releases/",
    resolvers += "Scala tools snapshots" at "http://scala-tools.org/repo-snapshots",
    resolvers += "Sonatype Repository" at "http://oss.sonatype.org/content/groups/public/",
    resolvers += "JBoss repository" at "http://repository.jboss.org/nexus/content/repositories/",
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
    resolvers += "Typesafe Repository ide-2.9" at "http://repo.typesafe.com/typesafe/simple/ide-2.9/",
    resolvers += "Twitter Repository" at "http://maven.twttr.com/",
    resolvers += "Akka Repository" at "http://akka.io/snapshots/"
  )

  private val LicenseFile = """(license|licence|notice|copying)([.]\w+)?$""".r
  private def isLicenseFile(fileName: String): Boolean =
    fileName.toLowerCase match {
      case LicenseFile(_, ext) if ext != ".class" => true // DISLIKE
      case _ => false
    }

  private val ReadMe = """(readme)([.]\w+)?$""".r
  private def isReadme(fileName: String): Boolean =
    fileName.toLowerCase match {
      case ReadMe(_, ext) if ext != ".class" => true
      case _ => false
    }

  private object PathList {
    private val sysFileSep = System.getProperty("file.separator")
    def unapplySeq(path: String): Option[List[String]] = {
      val split = path.split(if (sysFileSep.equals( """\""")) """\\""" else sysFileSep)
      if (split.size == 0) None
      else Some(split.toList)
    }
  }

  lazy val extAssemblySettings = scala.Seq[sbt.Project.Setting[_]](
    jarName in assembly <<= (name, version) { (name, version) => "market-capture-" + version + ".jar" } ,
    test in assembly := {},

    mergeStrategy in assembly := {
      case PathList(ps @ _*) if isReadme(ps.last) || isLicenseFile(ps.last) =>
        MergeStrategy.rename
      case PathList("META-INF", xs @ _*) =>
        (xs.map {_.toLowerCase}) match {
          case ("manifest.mf" :: Nil) => MergeStrategy.discard
          case list @ (head :: tail) if (list.reverse.head == "manifest.mf") => MergeStrategy.discard
          case list @ (head :: tail) if (list.reverse.head == "notice.txt") => MergeStrategy.discard
          case "plexus" :: _ => MergeStrategy.discard
          case "maven" :: _ => MergeStrategy.discard
          case e => MergeStrategy.deduplicate
        }
      case _ => MergeStrategy.deduplicate
    }
  )
}

object Dependencies {
  import Dependency._

  val capture = Seq(scalaz, finagleKestrel, marketDb, casbah, ostrich, scalaIO, Test.akkaTestkit, Test.mockito, Test.scalatest, Test.scalacheck)

  val quant = Seq(Test.akkaTestkit, Test.mockito, Test.scalatest, Test.scalacheck)

  val engine = Seq(Test.akkaTestkit, Test.mockito, Test.scalatest, Test.scalacheck)

  val core = Seq(Test.akkaTestkit, Test.mockito, Test.scalatest, Test.scalacheck)

  val plaza2 = Seq(ostrich, plaza2Connectivity, akka, scalaTime, slf4jApi, logback, jodaTime, jodaConvert) ++
    Seq(Test.akkaTestkit, Test.mockito, Test.scalatest, Test.scalacheck)
}


object Dependency {

  // Versions

  object V {
    val MarketDb     = "0.1-SNAPSHOT"
    val Plaza2       = "0.1-SNAPSHOT"

    val Scalatest    = "1.6.1"
    val Slf4j        = "1.6.4"
    val Mockito      = "1.8.1"
    val Scalacheck   = "1.9"
    val Scalaz       = "6.0.4"
    val Logback      = "1.0.0"
    val ScalaSTM     = "0.4"
    val JodaTime     = "2.0"
    val JodaConvert  = "1.2"
    val SBinary      = "0.4.0"
    val ScalaTime    = "0.5"
    val Akka         = "2.0"
    val ScalaIO      = "0.3.0"
    val Casbah       = "2.1.5-1"

    // Twitter dependencies
    val Finagle      = "4.0.2"
    val Ostrich      = "7.0.0"
  }

  // Compile
  val plaza2Connectivity     = "com.ergodicity.connectivity"      %% "plaza2"                 % V.Plaza2
  val marketDb               = "com.ergodicity.marketdb"          %% "marketdb-api"           % V.MarketDb

  val slf4jApi               = "org.slf4j"                         % "slf4j-api"              % V.Slf4j
  val logback                = "ch.qos.logback"                    % "logback-classic"        % V.Logback
  val scalaz                 = "org.scalaz"                       %% "scalaz-core"            % V.Scalaz
  val scalaSTM               = "org.scala-tools"                  %% "scala-stm"              % V.ScalaSTM
  val jodaTime               = "joda-time"                         % "joda-time"              % V.JodaTime
  val jodaConvert            = "org.joda"                          % "joda-convert"           % V.JodaConvert
  val finagleCore            = "com.twitter"                      %% "finagle-core"           % V.Finagle
  val finagleKestrel         = "com.twitter"                      %% "finagle-kestrel"        % V.Finagle
  val ostrich                = "com.twitter"                      %% "ostrich"                % V.Ostrich
  val sbinary                = "org.scala-tools.sbinary"          %% "sbinary"                % V.SBinary
  val scalaTime              = "org.scala-tools.time"             %% "time"                   % V.ScalaTime intransitive()
  val akka                   = "com.typesafe.akka"                 % "akka-actor"             % V.Akka
  val scalaIO                = "com.github.scala-incubator.io"    %% "scala-io-core"          % V.ScalaIO
  val casbah                 = "com.mongodb.casbah"               %% "casbah"                 % V.Casbah

  // Provided

  object Provided {

  }

  // Runtime

  object Runtime {

  }

  // Test

  object Test {
    val mockito        = "org.mockito"                 % "mockito-all"                   % V.Mockito      % "test"
    val scalatest      = "org.scalatest"              %% "scalatest"                     % V.Scalatest    % "it,test"
    val scalacheck     = "org.scala-tools.testing"    %% "scalacheck"                    % V.Scalacheck   % "test"
    val akkaTestkit    = "com.typesafe.akka"           % "akka-testkit"                  % V.Akka         % "it, test"
  }
}