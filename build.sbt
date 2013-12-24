name := "test"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "avalon-framework" % "avalon-framework-api" % "4.2.0",
  "avalon-framework" % "avalon-framework-impl" % "4.2.0",
  "commons-codec" % "commons-codec" % "1.7",
  "org.apache.xmlgraphics" % "fop" % "1.1" excludeAll(
    ExclusionRule(organization = "org.apache.avalon.framework")
    ),
  "xalan" % "xalan" % "2.7.1"
)     

play.Project.playJavaSettings
