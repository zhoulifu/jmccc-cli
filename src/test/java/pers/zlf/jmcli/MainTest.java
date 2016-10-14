package pers.zlf.jmcli;

import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.cli.ParseException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.launch.LaunchException;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.WindowSize;

import pers.zlf.jmcli.util.AuthenticatorProxy;

public class MainTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Before
    public void before() throws IOException {
        File version = new File(temp.getRoot(), "/versions/1.7.10");
        version.mkdirs();
        Files.copy(getClass().getClassLoader().getResourceAsStream("1.7.10.json"),
                   version.toPath().resolve("1.7.10.json"));
    }

    @Test
    public void testArgParser() throws LaunchException, ParseException, IOException {
        String args = "-u foo -o -f --min-heap 128 --max-heap 512 " +
                      temp.getRoot().getCanonicalPath();
        LaunchOption opt = Main.parse(args.split(" "));

        assertEquals(opt.getMaxMemory(), 512);
        assertEquals(opt.getMinMemory(), 128);
        assertEquals(opt.getVersion().getVersion(), "1.7.10");
        assertEquals(opt.getWindowSize(), WindowSize.fullscreen());

        AuthenticatorProxy authenticator = (AuthenticatorProxy) opt.getAuthenticator();
        assertTrue(authenticator.isOfflineMode());

        AuthInfo info = authenticator.auth();
        assertEquals(info.getUsername(), "foo");
    }

    @Test
    public void testException() throws LaunchException, ParseException, IOException {
        String args = "-V 1.8 " + temp.getRoot().getCanonicalPath();
        try {
            Main.parse(args.split(" "));
        } catch (LaunchException e) {
            assertThat(e.getMessage(), endsWith("Version not found: 1.8"));
        }

        try {
            Main.parse(new String[0]);
        } catch (Exception e) {
            assertThat(e, IsInstanceOf.instanceOf(ParseException.class));
        }
    }
}
