package net.wade.autotests.utilities;

import net.wade.autotests.core.Kafka;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@PropertySource("/autotests.properties")
@PropertySource("/environments/autotests_${environment}.properties")
public class KafkaManager {

    @Value("${settings.kafka.bootstrapServer}")
    private String settingsKafkaBootstrapServer;

    @Autowired
    private EnvironmentResolver environmentResolver;

    private Kafka kafka;

    public void createKafka(String groupId, String clientId) {
        kafka = new Kafka.Builder()
                .withBootstrapServer(settingsKafkaBootstrapServer)
                .withValueSerializer(ByteArraySerializer.class)
                .withValueDeserializer(ByteArrayDeserializer.class)
                .withKeySerializer(StringSerializer.class)
                .withKeyDeserializer(StringDeserializer.class)
                .withGroupId(groupId)
                .build();
    }

    public Kafka getKafka() {
        return kafka;
    }

    private void setTopic(String topic) {
        kafka.setTopic(environmentResolver.getKafkaTopic(topic));
    }

    public Map<Object, Object> readFromTopic(String topic) {
        setTopic(topic);
        return kafka.readWithKey();
    }

    public Map<Object, Object> readFromTopicWithOffset(String topic, int offset) {
        setTopic(topic);
        return kafka.readWithOffset(offset);
    }

    public Map<Object, Object> readFromTopicWithObject(String topic) {
        setTopic(topic);
        return kafka.readWithObject();
    }

    public  void sendToTopic(Object key, Object message, String topic) {
        setTopic(topic);
        if (key == null) {
            kafka.send(message);
        } else {
            kafka.sendWithKey(key, message);
        }
    }

    public void sendToTopic(Object message, String topic) {
        sendToTopic(null, message, topic);
    }
}
