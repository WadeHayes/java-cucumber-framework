package net.wade.autotests.utilities;

import org.awaitility.core.ConditionTimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.given;

public class Waiter {
    public static void waitKafka(Callable<Boolean> runnable, int seconds, int interval) {
        try {
            given()
                    .pollInterval(interval, TimeUnit.SECONDS)
                    .await()
                    .pollThread(Thread::new)
                    .atMost(seconds, TimeUnit.SECONDS)
                    .until(runnable);
        } catch (Exception e) {
            if (!(e instanceof ConditionTimeoutException)) {
                throw e;
            }
        }
    }

    public static void waitKafka(Callable<Boolean> runnable, int seconds) {
        waitKafka(runnable, seconds, /* дописать получение проперти kafka.poll.interval.seconds */1);
    }

    public static void waitKafka(Callable<Boolean> runnable) {
        waitKafka(runnable, /* дописать получение проперти kafka.timeout.waiter.seconds */1, /* дописать получение проперти kafka.poll.interval.seconds */1);
    }

    public static void waitCassandra(Callable<Boolean> runnable, int seconds) {
        try {
            given()
                    .pollInterval(/* дописать получение проперти cassandra.poll.interval.seconds */1, TimeUnit.SECONDS)
                    .await()
                    .pollThread(Thread::new)
                    .atMost(seconds, TimeUnit.SECONDS)
                    .until(runnable);
        } catch (Exception e) {
            if (!(e instanceof ConditionTimeoutException)) {
                throw e;
            }
        }
    }

    public static void waitCassandra(Callable<Boolean> runnable) {
        waitCassandra(runnable, /* дописать получение проперти cassandra.timeout.waiter.seconds */1);
    }

    public static void waitCeph(Callable<Boolean> runnable, int seconds) {
        try {
            given()
                    .pollInterval(/* дописать получение проперти ceph.poll.interval.seconds */1, TimeUnit.SECONDS)
                    .await()
                    .pollThread(Thread::new)
                    .atMost(seconds, TimeUnit.SECONDS)
                    .until(runnable);
        } catch (Exception e) {
            if (!(e instanceof ConditionTimeoutException)) {
                throw e;
            }
        }
    }

    public static void waitCeph(Callable<Boolean> runnable) {
        waitCeph(runnable, /* дописать получение проперти ceph.timeout.waiter.seconds */1);
    }
}
