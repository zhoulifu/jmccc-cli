package pers.zlf.jmcli.util;

import org.to2mbn.jmccc.auth.AuthInfo;
import org.to2mbn.jmccc.auth.AuthenticationException;
import org.to2mbn.jmccc.auth.Authenticator;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.auth.yggdrasil.YggdrasilAuthenticator;

public class AuthenticatorProxy implements Authenticator {
    private static String DEFAULT_PLAYER_NAME = "Hello Minecraft";

    private Authenticator authenticator;
    private String username;
    private String password;
    private boolean offlineMode;

    public AuthenticatorProxy(String username, String password, boolean offline) {
        this.username = username;
        this.password = password;
        this.offlineMode = offline;
    }

    @Override
    public AuthInfo auth() throws AuthenticationException {
        return getAuthenticator().auth();
    }

    private Authenticator getAuthenticator() throws AuthenticationException {
        if (authenticator == null) {
            authenticator = create();
        }

        return authenticator;
    }

    private synchronized Authenticator create() throws AuthenticationException {
        if (authenticator != null) {
            return authenticator;
        }

        return offlineMode ? new OfflineAuthenticator(
                (username == null ? DEFAULT_PLAYER_NAME : username))
                           : YggdrasilAuthenticator.password(username, password);
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }
}
