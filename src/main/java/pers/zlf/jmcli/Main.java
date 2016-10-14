package pers.zlf.jmcli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.to2mbn.jmccc.auth.Authenticator;
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

import pers.zlf.jmcli.util.AuthenticatorProxy;
import pers.zlf.jmcli.util.LogCallback;
import pers.zlf.jmcli.util.VersionComparator;

public class Main {
    private static String USAGE = "java -jar jmccc-cli.jar [OPTION]... /path/to/" +
                                  ".minecraft";
    private static Options OPTIONS;
    private static Launcher LAUNCHER = LauncherBuilder.buildDefault();
    private static HelpFormatter FORMATTER = new HelpFormatter();

    static {
        OPTIONS = new Options();
        OPTIONS.addOption(new Option("f", "full-screen", false, "full screen"));
        OPTIONS.addOption(new Option("o", "offline-mode", false, "offline mode"));
        OPTIONS.addOption(new Option("h", "print this help information"));
        OPTIONS.addOption(Option.builder("u").hasArg()
                                .desc("the username of Yggdrasil. (or player's name if " +
                                      "enable offline mode)").build());
        OPTIONS.addOption(Option.builder("p").hasArg().desc("the password of Yggdrasil")
                                .build());
        OPTIONS.addOption(Option.builder("V").hasArg()
                                .desc("launch specified minecraft version, if none " +
                                      "specified, launch the latest version provided in" +
                                      " .minecraft/versions").build());
        OPTIONS.addOption(Option.builder(null).longOpt("min-heap").argName("size")
                                .desc("set initial Java heap size to SIZE Mb for " +
                                      "launching minecraft").hasArg().build());
        OPTIONS.addOption(Option.builder(null).longOpt("max-heap").argName("size")
                                .desc("set maximum Java heap size to SIZE Mb for " +
                                      "launching minecraft").hasArg().build());
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        try {
            LaunchOption opt = parse(args);
            try {
                LAUNCHER.launch(opt);
            } catch (LaunchException e) {
                if (e instanceof MissingDependenciesException) {
                    System.out.println("Missing Library: " + e.getMessage());
                    downloadMissingJar(opt.getMinecraftDirectory(),
                                       ((MissingDependenciesException) e)
                                               .getMissingLibraries());
                    System.out.println("Trying to relaunch minecraft");
                    LAUNCHER.launch(opt);
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof ParseException) {
                FORMATTER.printHelp(USAGE, Main.OPTIONS);
            }
        }
    }

    private static void downloadMissingJar(MinecraftDirectory dir,
            Set<Library> libraries) throws InterruptedException {
        if (libraries == null || libraries.size() == 0) {
            return;
        }

        MinecraftDownloader downloader = MinecraftDownloaderBuilder.buildDefault();
        CountDownLatch cdl = new CountDownLatch(libraries.size());

        for (Library library : libraries) {
            System.out.println("Downloading Library " + library);
            downloader.download(downloader.getProvider().library(dir, library),
                                new LogCallback(cdl, library));
        }

        cdl.await();
        downloader.shutdown();
    }

    private static LaunchOption parse(
            String[] args) throws LaunchException, ParseException {
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(Main.OPTIONS, args);

        if (cmd.hasOption("h")) {
            FORMATTER.printHelp(USAGE, OPTIONS);
            System.exit(0);
        }

        String[] path = cmd.getArgs();
        if (path == null || path.length == 0) {
            throw new ParseException("Missing Minecraft directory");
        }

        MinecraftDirectory dir = new MinecraftDirectory(path[0]);
        Authenticator auth = new AuthenticatorProxy(cmd.getOptionValue('u'),
                                                    cmd.getOptionValue('p'),
                                                    cmd.hasOption('o'));
        String version = cmd.hasOption('V') ? cmd.getOptionValue('V') : latestVersion(
                dir);
        if (version == null || version.trim().length() == 0) {
            throw new LaunchException("Version directory not existed");
        }

        LaunchOption opt;
        try {
            opt = new LaunchOption(version, auth, dir);
        } catch (IOException | IllegalArgumentException e) {
            throw new LaunchException(e);
        }

        if (cmd.hasOption("f")) {
            opt.setWindowSize(WindowSize.fullscreen());
        }

        if (cmd.hasOption("min-heap")) {
            opt.setMinMemory(Integer.valueOf(cmd.getOptionValue("min-heap")));
        }

        if (cmd.hasOption("max-heap")) {
            opt.setMaxMemory(Integer.valueOf(cmd.getOptionValue("max-heap")));
        }

        return opt;
    }

    @SuppressWarnings("all")
    private static String latestVersion(MinecraftDirectory dir) throws LaunchException {
        File versionsDir = dir.getVersions();
        if (!versionsDir.exists() || !versionsDir.isDirectory()) {
            throw new LaunchException("Versions directory not existed");
        }

        String[] vers = versionsDir.list();
        if (vers.length == 0) {
            return null;
        }

        Arrays.sort(vers, new VersionComparator());
        return vers[vers.length - 1];
    }
}
