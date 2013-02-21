import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "XSL-tester"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "org.apache.xmlgraphics" % "fop" % "1.0",
    "net.sf.saxon" % "Saxon-HE" % "9.4"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
