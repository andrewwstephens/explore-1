import sbtcrossproject.crossProject
import sbtcrossproject.CrossType
import Settings.Libraries._

val reactJS = "16.13.1"
val FUILess = "2.8.7"

parallelExecution in (ThisBuild, Test) := false

cancelable in Global := true

Global / onChangedBuildSource := ReloadOnSourceChanges

addCommandAlias(
  "exploreWDS",
  "; explore/fastOptJS::stopWebpackDevServer; explore/fastOptJS::startWebpackDevServer; ~explore/fastOptJS"
)

addCommandAlias(
  "constraintsWDS",
  "; constraints/fastOptJS::stopWebpackDevServer; constraints/fastOptJS::startWebpackDevServer; ~constraints/fastOptJS"
)

addCommandAlias(
  "targeteditorWDS",
  "; targeteditor/fastOptJS::stopWebpackDevServer; targeteditor/fastOptJS::startWebpackDevServer; ~targeteditor/fastOptJS"
)

addCommandAlias(
  "observationtreeWDS",
  "; observationtree/fastOptJS::stopWebpackDevServer; observationtree/fastOptJS::startWebpackDevServer; ~observationtree/fastOptJS"
)

addCommandAlias(
  "proposalWDS",
  "; proposal/fastOptJS::stopWebpackDevServer; proposal/fastOptJS::startWebpackDevServer; ~proposal/fastOptJS"
)

addCommandAlias(
  "stopWDS",
  "fastOptJS::stopWebpackDevServer"
)

addCommandAlias(
  "quickTest",
  "modelJVM/test"
)

addCommandAlias(
  "fixImports",
  "; scalafix OrganizeImports"
)

// For Heroku deployment
val stage = taskKey[Unit]("Stage and clean task")

stage := {
  (explore / Compile / fullOptJS / webpack).value
  // https://devcenter.heroku.com/articles/reducing-the-slug-size-of-play-2-x-applications#using-sbt-to-clean-build-artifacts
  // If needed, caches can be purged manually: https://thoughtbot.com/blog/how-to-reduce-a-large-heroku-compiled-slug-size
  if (sys.env.getOrElse("POST_STAGE_CLEAN", "false").equals("true")) {
    println("Cleaning up...")
    // Remove sbt-scalajs-bundler directory, which includes node_modules.
    val bundlerDir       =
      (explore / Compile / fullOptJS / artifactPath).value.getParentFile.getParentFile
    sbt.IO.delete(bundlerDir)
    // Remove coursier cache
    val coursierCacheDir = csrCacheDirectory.value
    sbt.IO.delete(coursierCacheDir)
  }
}

lazy val root = project
  .in(file("."))
  .settings(name := "explore-root")
  .settings(commonSettings: _*)
  .aggregate(model.jvm,
             model.js,
             common,
             constraints,
             targeteditor,
             observationtree,
             proposal,
             explore
  )

lazy val model = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("model"))
  .settings(commonSettings: _*)
  .settings(commonLibSettings: _*)
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val common = project
  .in(file("common"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(
    npmDependencies in Compile ++= Seq(
      "loglevel" -> "1.6.8"
    ),
    libraryDependencies ++=
      ReactCommon.value,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoKeys ++= Seq[BuildInfoKey](
      "ODBEndpoint" -> sys.env.getOrElse("ODB_ENDPOINT",
                                         "wss://explore-hasura.herokuapp.com/v1/graphql"
      )
    ),
    buildInfoPackage := "explore"
  )
  .enablePlugins(ScalaJSBundlerPlugin, BuildInfoPlugin)
  .dependsOn(model.js)

lazy val targeteditor = project
  .in(file("targeteditor"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(commonWDS: _*)
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common)
  .settings(
    libraryDependencies ++=
      GeminiLocales.value ++
        ReactAladin.value ++
        ReactDatepicker.value ++
        ReactHighcharts.value ++
        ReactSizeMe.value
  )

lazy val constraints = project
  .in(file("constraints"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(commonWDS: _*)
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common)

lazy val observationtree = project
  .in(file("observationtree"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(commonWDS: _*)
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common)
  .settings(
    libraryDependencies ++=
      ReactAtlasKitTree.value,
    npmDependencies in Compile ++= Seq(
      "core-js" -> "2.6.11" // Without this, core-js 3 is used, which conflicts with @babel/runtime-corejs2
    ),
    scalaJSLinkerConfig in (Compile, fastOptJS) ~= { _.withSourceMap(false) },
    scalaJSLinkerConfig in (Compile, fullOptJS) ~= { _.withSourceMap(false) }
  )

lazy val proposal = project
  .in(file("proposal"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(commonWDS: _*)
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(common)

lazy val explore: Project = project
  .in(file("explore"))
  .settings(commonSettings: _*)
  .settings(commonJsLibSettings: _*)
  .settings(commonWDS: _*)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    libraryDependencies ++=
      ReactCommon.value ++
        ReactGridLayout.value ++
        ReactHighcharts.value ++
        ReactResizable.value ++
        ReactSizeMe.value
  )
  .dependsOn(constraints, targeteditor, observationtree, proposal)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
  description := "Explore",
  homepage := Some(url("https://github.com/geminihlsw/explore")),
  licenses := Seq("BSD 3-Clause License" -> url("https://opensource.org/licenses/BSD-3-Clause")),
  scalacOptions += "-Ymacro-annotations",
  scalacOptions ~= (_.filterNot(
    Set(
      // By necessity facades will have unused params
      "-Wdead-code",
      "-Wunused:params"
    )
  )),
  // don't publish anything
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  Keys.`package` := file("")
)

lazy val commonLibSettings = Seq(
  libraryDependencies ++=
    LucumaCore.value ++
      Cats.value ++
      Mouse.value ++
      CatsEffect.value ++
      Monocle.value ++
      Circe.value ++
      Crystal.value ++
      Clue.value ++
      In(Test)(
        MUnit.value ++
          Discipline.value ++
          MonocleLaw.value ++
          LucumaCoreTestKit.value
      ),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val commonJsLibSettings = lucumaScalaJsSettings ++ commonLibSettings ++ Seq(
  libraryDependencies ++=
    ScalaJSReact.value ++
      ReactSemanticUI.value ++
      ClueScalaJS.value ++
      LucumaUI.value ++
      Log4Cats.value ++
      In(Test)(
        ScalaJSReactTest.value
      ),
  dependencyOverrides ++= ScalaJSReact.value
)

lazy val commonWDS = Seq(
  webpack / version := "4.44.1",
  startWebpackDevServer / version := "3.11.0",
  fastOptJS / webpackConfigFile := Some(
    (common / Compile / sourceDirectory).value / "webpack" / "dev.webpack.config.js"
  ),
  fullOptJS / webpackConfigFile := Some(
    (common / Compile / sourceDirectory).value / "webpack" / "prod.webpack.config.js"
  ),
  version in installJsdom := "16.4.0",
  webpackMonitoredDirectories += (common / Compile / resourceDirectory).value,
  webpackMonitoredDirectories += ((common / Compile / sourceDirectory).value / "webpack"),
  webpackResources := ((common / Compile / sourceDirectory).value / "webpack") * "*.js",
  webpackMonitoredFiles / includeFilter := "*",
  useYarn := true,
  fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
  fullOptJS / webpackBundlingMode := BundlingMode.Application,
  test := {},
  Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
  Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
  // NPM libs for development, mostly to let webpack do its magic
  npmDevDependencies in Compile ++= Seq(
    "postcss-loader"                     -> "3.0.0",
    "autoprefixer"                       -> "9.8.5",
    "url-loader"                         -> "4.1.0",
    "file-loader"                        -> "6.0.0",
    "css-loader"                         -> "3.5.3",
    "style-loader"                       -> "1.2.1",
    "less"                               -> "3.11.1",
    "less-loader"                        -> "6.1.0",
    "sass"                               -> "1.26.10",
    "sass-loader"                        -> "9.0.2",
    "webpack-merge"                      -> "4.2.2",
    "mini-css-extract-plugin"            -> "0.9.0",
    "webpack-dev-server-status-bar"      -> "1.1.2",
    "cssnano"                            -> "4.1.10",
    "terser-webpack-plugin"              -> "3.0.6",
    "html-webpack-plugin"                -> "4.3.0",
    "optimize-css-assets-webpack-plugin" -> "5.0.3",
    "favicons-webpack-plugin"            -> "3.0.1",
    "@packtracker/webpack-plugin"        -> "2.3.0"
  ),
  npmDependencies in Compile ++= Seq(
    "react"             -> reactJS,
    "react-dom"         -> reactJS,
    "react-is"          -> reactJS,
    "fomantic-ui-less"  -> FUILess,
    "prop-types"        -> "15.7.2",
    "react-moon"        -> "2.0.1",
    "styled-components" -> "5.1.1"
  )
)
