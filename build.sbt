// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "io.pig"
ThisBuild / organizationName := "Pig.io"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("valencik", "Andrew Valencik")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := false

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

// publish snapshots from main branch
ThisBuild / tlCiReleaseBranches := Seq("main")

// use JDK 11
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))

val Scala213 = "2.13.8"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.1.1")
ThisBuild / scalaVersion := Scala213 // the default Scala

val catsV = "2.8.0"
val catsEffectV = "3.3.14"
val fs2V = "3.2.12"
val munitCatsEffectV = "1.0.7"
val luceneV = "9.3.0"

lazy val root = tlCrossRootProject.aggregate(lucene, example)

lazy val lucene = project
  .in(file("lucene"))
  .settings(
    name := "textmogrify-lucene",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsV,
      "org.typelevel" %% "cats-effect" % catsEffectV,
      "co.fs2" %% "fs2-core" % fs2V,
      "co.fs2" %% "fs2-io" % fs2V,
      "org.apache.lucene" % "lucene-core" % luceneV,
      "org.apache.lucene" % "lucene-analysis-common" % luceneV,
      "org.typelevel" %% "munit-cats-effect-3" % munitCatsEffectV % Test,
    ),
  )

lazy val example = project
  .in(file("example"))
  .enablePlugins(NoPublishPlugin)
  .dependsOn(lucene)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(lucene)
  .settings(
    tlSiteRelatedProjects := Seq(
      "lucene" -> url("https://lucene.apache.org/"),
      TypelevelProject.CatsEffect,
      TypelevelProject.Fs2,
    )
  )
