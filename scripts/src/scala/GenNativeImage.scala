package bleep
package scripts

import bleep.plugin.nativeimage.NativeImagePlugin

object GenNativeImage extends BleepScript("GenNativeImage"):
  def run(started: Started, commands: Commands, args: List[String]): Unit =
    // First arg is the project, second if present is the binary name
    if args.isEmpty then
      started.logger.error("Please provide the project name for the generated native-image")
      return
    started.logger.info(s"Generating native-image for ${args.head}")
    val projectName = model.CrossProjectName(model.ProjectName(args.head), crossId = None)
    val project     = started.bloopProject(projectName)
    commands.compile(List(projectName))

    val plugin = new NativeImagePlugin(
      project = project,
      logger = started.logger,
      jvmCommand = started.jvmCommand,
      nativeImageOptions = List(
        "--exclude-config",
        "/.*.jar",
        ".*.properties",
      ) ++ (if sys.props.get("os.name").contains("Linux") then List("--static") else List.empty),
      env = sys.env.toList ++ List(("USE_NATIVE_IMAGE_JAVA_PLATFORM_MODULE_SYSTEM", "false")),
    ):
      // allow user to pass in name of generated binary as parameter
      override val nativeImageOutput = args.lift(1) match
        case Some(relPath) =>
          // smoothen over some irritation from github action scripts
          val relPathNoExe = if relPath.endsWith(".exe") then relPath.dropRight(".exe".length) else relPath
          val file         = if relPathNoExe == args.head then s"$relPathNoExe-native" else relPathNoExe
          started.buildPaths.cwd / file
        case None => super.nativeImageOutput
    val path = plugin.nativeImage()
    started.logger.info(s"Created native-image at $path")
