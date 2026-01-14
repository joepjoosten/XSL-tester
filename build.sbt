name := "xsl-tester"
organization := "net.xsltransform"
version := "2.0.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)

scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  guice,
  javaJpa,
  "org.hibernate.orm" % "hibernate-core" % "6.4.4.Final",
  "org.mariadb.jdbc" % "mariadb-java-client" % "3.3.3",
  "org.apache.xmlgraphics" % "fop" % "2.11",
  "commons-codec" % "commons-codec" % "1.16.1",
  "net.sf.saxon" % "Saxon-HE" % "12.5",
  "xalan" % "xalan" % "2.7.3"
)

// Ensure persistence.xml is not externalized
PlayKeys.externalizeResourcesExcludes += baseDirectory.value / "conf" / "META-INF" / "persistence.xml"

// Java 21
javacOptions ++= Seq("-source", "21", "-target", "21")
