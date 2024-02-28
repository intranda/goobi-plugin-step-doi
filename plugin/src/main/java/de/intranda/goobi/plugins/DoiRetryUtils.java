package de.intranda.goobi.plugins;

import java.time.Duration;
import java.util.concurrent.Callable;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DoiRetryUtils {
    public static <V, E extends Throwable> V retry(E exception, Duration wait, int maxRetries, Callable<V> callable) throws E {
        int counter = 0;
        while (counter < maxRetries) {
            try {
                return callable.call();
            } catch (Exception e) {
                counter++;
                log.error(e);
                try {
                    Thread.sleep(wait.toMillis() * counter);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw exception;
    }
}
