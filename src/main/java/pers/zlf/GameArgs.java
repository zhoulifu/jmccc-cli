package pers.zlf;

class GameArgs {
    private String mcVer;
    private String directoryPath;
    private String username;
    private String password;
    private boolean offlineMode;
    private boolean fullScreen;

    public String getMcVer() {
        return mcVer;
    }

    public void setMcVer(String mcVer) {
        this.mcVer = mcVer;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void offlineMode() {
        this.offlineMode = true;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void fullScreen() {
        this.fullScreen = true;
    }

    @Override
    public String toString() {
        return "GameArgs{" +
               "mcVer='" + mcVer + '\'' +
               ", directoryPath='" + directoryPath + '\'' +
               ", username='" + username + '\'' +
               ", password='" + password + '\'' +
               ", offlineMode=" + offlineMode +
               ", fullScreen=" + fullScreen +
               '}';
    }
}
