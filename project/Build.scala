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
    "avalon-framework" % "avalon-framework-api" % "4.2.0",
    "avalon-framework" % "avalon-framework-impl" % "4.2.0",
    "commons-codec" % "commons-codec" % "1.7",
    "org.apache.xmlgraphics" % "fop" % "1.1" excludeAll(
      ExclusionRule(organization = "org.apache.avalon.framework")
      ),
    "net.sf.saxon" % "Saxon-HE" % "9.4"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
