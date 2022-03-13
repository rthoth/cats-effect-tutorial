ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps"
)

lazy val root = (project in file("."))
  .settings(
    name := "cats-effect-tutorial"
  )
  .aggregate(
    fcopy
  )

lazy val fcopy = (project in file("fcopy"))
  .settings(
    name := "fcopy",
    description := "It's a functional file copying program.",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.3.7" withSources() withJavadoc()
    )
  )
