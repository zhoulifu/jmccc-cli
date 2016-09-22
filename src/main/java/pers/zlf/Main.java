package pers.zlf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;
import org.to2mbn.jmccc.launch.LaunchException;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.MissingDependenciesException;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;
import org.to2mbn.jmccc.option.WindowSize;
import org.to2mbn.jmccc.version.Library;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.jmccc.version.parsing.Versions;

public class Main {
    private static String DEFAULT_PLAYER_NAME = "hello world";
    private static String USAGE = "java -jar packaged.jar [OPTION]... /path/to/" +
                                  ".minecraft";
    static Options OPTIONS;
    static Launcher LAUNCHER = LauncherBuilder.buildDefault();
    static HelpFormatter FORMATTER = new HelpFormatter();

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(new Option("f", "full-screen", false, "full screen"));
        OPTIONS.addOption(new Option("o", "offline-mode", false, "offline mode"));
        OPTIONS.addOption(new Option("h", "print this help information"));
        OPTIONS.addOption(Option.builder("u").hasArg().numberOfArgs(1)
                                .desc("the username of Yggdrasil. (or player's name if " +
                                      "enable offline mode)").build());
        OPTIONS.addOption(Option.builder("p").hasArg().numberOfArgs(1)
                                .desc("the password of Yggdrasil").build());
        OPTIONS.addOption(Option.builder("V").hasArg().numberOfArgs(1)
                                .desc("run specified minecraft version,if none " +
                                      "specified, run the latest version in " +
                                      ".minecraft/versions").build());
    }

    public static void main(String[] args) throws Exception {

        try {
            GameArgs gameArgs = parse(args);
            if (gameArgs != null) {
                launch(gameArgs, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ParseException) {
                FORMATTER.printHelp(USAGE, Main.OPTIONS);
            }
        }
    }

    private static void launch(GameArgs args,
            boolean relaunch) throws IOException, ExecutionException,
            InterruptedException, LaunchException {
        MinecraftDirectory dir = new MinecraftDirectory(args.getDirectoryPath());

        Authenticator auth;
        if (args.isOfflineMode()) {
            auth = new OfflineAuthenticator(
                    args.getUsername() == null ? DEFAULT_PLAYER_NAME
                                               : args.getUsername());
        } else {
            auth = YggdrasilAuthenticator.password(args.getUsername(),
                                                   args.getPassword());
        }

        String verStr;
        String presentVer = args.getMcVer();
        if (presentVer == null || presentVer.length() == 0) {
            File versionsDir = dir.getVersions();
            if (!versionsDir.exists() || !versionsDir.isDirectory()) {
                throw new LaunchException("versions directory not existed");
            }

            String[] vers = versionsDir.list();
            Arrays.sort(vers, new VersionComparator());
            verStr = vers[vers.length - 1];
        } else {
            verStr = presentVer;
        }

        Version version = Versions.resolveVersion(dir, verStr);
        if (version == null) {
            throw new LaunchException("Version directory not existed: " + verStr);
        }

        LaunchOption opt = new LaunchOption(version, auth, dir);

        if (args.isFullScreen()) {
            opt.setWindowSize(WindowSize.fullscreen());
        }

        try {
            LAUNCHER.launch(opt);
        } catch (LaunchException e) {
            if ((e instanceof MissingDependenciesException) && !relaunch) {
                downloadMissingJar(dir,
                                   ((MissingDependenciesException) e)
                                           .getMissingLibraries());
                launch(args, true);
            } else {
                throw e;
            }
        }
    }

    private static void downloadMissingJar(MinecraftDirectory dir,
            Set<Library> libraries) throws ExecutionException, InterruptedException {
        MinecraftDownloader downloader = MinecraftDownloaderBuilder.buildDefault();

        for (Library library : libraries) {
            Future<Void> future = downloader.download(
                    downloader.getProvider().library(dir, library), null);
            future.get();
        }

        downloader.shutdown();
    }

    private static GameArgs parse(String[] args) throws Exception {
        DefaultParser parser = new DefaultParser();
        GameArgs ags = new GameArgs();
        CommandLine cmd = parser.parse(Main.OPTIONS, args);
        if (cmd.hasOption("h")) {
            FORMATTER.printHelp(USAGE, OPTIONS);
            return null;
        }

        if (cmd.hasOption('u')) {
            ags.setUsername(cmd.getOptionValue('u'));
        }

        if (cmd.hasOption('p')) {
            ags.setPassword(cmd.getOptionValue('p'));
        }

        if (cmd.hasOption("f")) {
            ags.fullScreen();
        }

        if (cmd.hasOption("o")) {
            ags.offlineMode();
        }

        if (cmd.hasOption("V")) {
            ags.setMcVer(cmd.getOptionValue("V"));
        }

        String[] path = cmd.getArgs();
        if (path == null || path.length == 0) {
            throw new ParseException("Missing Minecraft directory");
        }

        ags.setDirectoryPath(path[0]);

        return ags;
    }
}
