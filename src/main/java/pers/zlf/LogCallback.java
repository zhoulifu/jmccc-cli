package pers.zlf;

import java.util.concurrent.CountDownLatch;

import org.to2mbn.jmccc.mcdownloader.download.concurrent.CallbackAdapter;
import org.to2mbn.jmccc.version.Library;

public class LogCallback extends CallbackAdapter<Void> {
    private final CountDownLatch cdl;
    private Library library;

    public LogCallback(CountDownLatch cdl, Library library) {
        this.cdl = cdl;
        this.library = library;
    }

    @Override
    public void done(Void result) {
        cdl.countDown();
        System.out.println("[" + library.toString() +"] has been downloaded successfully");
    }

    @Override
    public void failed(Throwable e) {
        cdl.countDown();
        System.out.println("Exception occurred while downloading [" + library.toString() +
              "] due to " + e.toString());
    }

    @Override
    public void cancelled() {
        cdl.countDown();
        System.out.println("Download task [" + library.toString() + "] has been cancelled");
    }
}
