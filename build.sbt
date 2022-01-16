


scalaVersion := "2.13.7"


name := "zio-peer-example"
organization := "searler"
version := "0.1"


val zio_version ="1.0.12"

libraryDependencies += "dev.zio" %% "zio" % zio_version
libraryDependencies += "dev.zio" %% "zio-streams" % zio_version
libraryDependencies += "dev.zio" %% "zio-test"          % zio_version % "test"
libraryDependencies +=  "dev.zio" %% "zio-test-sbt"      % zio_version % "test"
libraryDependencies += "dev.zio" %% "zio-json" % "0.1.5"

libraryDependencies += "io.github.searler" %% "zio-tcp" % "0.2.2"
libraryDependencies += "io.github.searler" %% "zio-peer" % "0.3.1"

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")


