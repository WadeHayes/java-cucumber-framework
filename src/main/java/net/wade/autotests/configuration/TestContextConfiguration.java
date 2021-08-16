package net.wade.autotests.configuration;

import net.wade.autotests.core.CassandraUtils;
import net.wade.autotests.core.Kafka;
import net.wade.autotests.core.AmazonS3Utils;
import net.wade.autotests.core.REST;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

@Configuration
@ContextConfiguration
@ComponentScan(basePackages = "net.wade.autotests")
@PropertySource("/autotests.properties")
@PropertySource("/environments/autotests_${environment}.properties")
public class TestContextConfiguration {

    @Value("${settings.amazon.pathStyleAccess}")
    private boolean settingsAmazonPathStyleAccess;

    @Value("${settings.amazon.accessKey}")
    private String settingsAmazonAccessKey;

    @Value("${settings.amazon.securityKey}")
    private String settingsAmazonSecurityKey;

    @Value("${settings.amazon.serviceEndpoint}")
    private String settingsAmazonServiceEndpoint;

    @Value("${settings.amazon.signingRegion}")
    private String settingsAmazonSigningRegion;

    @Value("${settings.cassandra.contactPoints}")
    private String settingsCassandraContactPoints;

    @Value("${settings.cassandra.localDataCenter}")
    private String settingsCassandraLocalDataCenter;

    @Value("${settings.kafka.bootstrapServer}")
    private String settingsKafkaBootstrapServer;


    @Bean
    public AmazonS3Utils amazon() {
        return new AmazonS3Utils(settingsAmazonPathStyleAccess, settingsAmazonAccessKey, settingsAmazonSecurityKey, settingsAmazonServiceEndpoint, settingsAmazonSigningRegion);
    }

    @Bean
    public REST rest() {
        return new REST();
    }

    @Bean
    public Kafka kafka() {
        return new Kafka.Builder()
                .withBootstrapServer(settingsKafkaBootstrapServer)
                .withValueSerializer(ByteArraySerializer.class)
                .withValueDeserializer(ByteArrayDeserializer.class)
                .withKeySerializer(StringSerializer.class)
                .withKeyDeserializer(StringDeserializer.class)
                .build();
    }

    @Bean
    public CassandraUtils CassandraUtils() {
        if (!settingsCassandraContactPoints.isEmpty()) {
            return new CassandraUtils(settingsCassandraContactPoints.split(","), settingsCassandraLocalDataCenter);
        }
        return null;
    }

}
