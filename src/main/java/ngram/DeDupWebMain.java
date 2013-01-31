package ngram;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: Oleksiy Pylypenko
 * Date: 12/14/11
 * Time: 2:11 PM
 */
public class DeDupWebMain {
    public  static DeDupWebMain MAIN_INSTANCE;

    private static final Logger log = Logger.getLogger("server");

    private SelectorThread threadSelector;
    private volatile boolean stopFlag;
    private static final long STOP_FILE_CHECK_TIMEOUT = 50;

    public static void main(String[] args) throws Exception {
        final URI baseUri = URI.create(args[0]);
        MAIN_INSTANCE = new DeDupWebMain(baseUri);
        MAIN_INSTANCE.waitStop();
    }

    public DeDupWebMain(URI baseUri) throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "com.reuters.core.ngram_deduplication.ddservice");
        log.info("Starting grizzly...");
        threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        log.info(String.format("Jersey app started with WADL available at %s/application.wadl", baseUri));
    }

    private void waitStop() {
        while (!stopFlag) {
            try {
                TimeUnit.SECONDS.sleep(STOP_FILE_CHECK_TIMEOUT);
            } catch (InterruptedException ex) {
                log.warn("Unexpected interrupted exception.", ex);
            }
        }
        threadSelector.stopEndpoint();
    }

    public void stop() {
        stopFlag = true;
    }
}
