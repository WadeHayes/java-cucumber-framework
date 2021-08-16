package net.wade.autotests.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("/autotests.properties")
@PropertySource("/environments/autotests_${environment}.properties")
public class EnvironmentResolver {

    private static final Logger logger = LogManager.getLogger(EnvironmentResolver.class);

    @Autowired
    Environment environment;

    public String getKafkaTopic(String topic) {
        if (environment.containsProperty("kafka" + topic)) {
            return environment.getProperty("kafka" + topic);
        }
        logger.error("'kafka." + topic + "' - не найдено! Будет использованно наименование топика из теста: '" + topic + "'");
        return topic;
    }

    public String getCEPHBucket(String bucketName) {
        String[] bucket = bucketName.split("/");

        if (bucket.length > 1) {
            String another = bucketName.replace(bucket[0], "");
            String newBucketName = bucket[0];

            if (environment.containsProperty("ceph." + newBucketName)) {
                return environment.getProperty("ceph." + newBucketName) + another;
            } else {
                logger.error("'ceph." + newBucketName + "' - не найдено! Будет использованно наименование бакета из теста: '" + newBucketName + "'");
                return newBucketName;
            }
        } else {
            if (environment.containsProperty("ceph." + bucketName)) {
                return environment.getProperty("ceph." + bucketName);
            }
        }

        logger.error("'ceph." + bucketName + "' - не найдено! Будет использованно наименование бакета из теста: '" + bucketName + "'");
        return bucketName;
    }
}
