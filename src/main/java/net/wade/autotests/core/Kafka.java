package net.wade.autotests.core;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class Kafka {

    private final KafkaProducerUtils producer;
    private final KafkaConsumerUtils consumer;
    private String topic;

    public Kafka() {
        producer = new KafkaProducerUtils(readProperties("producer."));
        consumer = new KafkaConsumerUtils(readProperties("consumer."));
    }

    public Kafka(Properties producerProperties, Properties consumerProperties) {
        this.producer = new KafkaProducerUtils(producerProperties);
        this.consumer = new KafkaConsumerUtils(consumerProperties);
    }

    /**
     * Отправка сообщения в кафка топик
     * @param message - сообщение
     */
    public void send(Object message) {
        sendWithKey(null, message);
    }

    /**
     * Отправляем сообщение в кафка топик с ключом
     *
     * @param key - ключ
     * @param message - сообщение
     */
    public void sendWithKey(Object key, Object message) {
        Object messageJSON = null;
        if (message instanceof String) {
            try {
                if (new File((String) message).isFile()) {
                    byte[] encoded = Files.readAllBytes(new File((String) message).toPath());
                    messageJSON = new String(encoded, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                messageJSON = message;
            }
        } else {
            messageJSON = message;
        }
        producer.sendMessageToTopic(key, topic, messageJSON);
    }

    public Map<Object, Object> readWithOffset(int offset) {
        return consumer.read(this.topic, KafkaReadType.KEY, 0, offset);
    }

    /**
     * Чтение топика кафка. Возвращает значение с id (0, 1, 2, 3 ...) вместо ключа
     *
     * @param offsetLag - количество оффсетов, на которое необходимо откатить счетчик назад
     * @return - пул сообщений Map<Object, Object>
     */
    public Map<Object, Object> readWithIterator(final int offsetLag) {
        return consumer.read(this.topic, KafkaReadType.ITERATOR, offsetLag, -1);
    }

    public Map<Object, Object> readWithIterator() {
        return consumer.read(this.topic, KafkaReadType.ITERATOR, /* дописать получение значения из проперти kafka.offset.lag */1, -1);
    }

    /**
     * Чтение топика кафка. Возвращает значение с ключами из кафки
     *
     * @param offsetLag - количество оффсетов, на которое необходимо откатить счетчик назад
     * @return - пул сообщений Map<Object, Object>
     */
    public Map<Object, Object> readWithKey(final int offsetLag) {
        return consumer.read(this.topic, KafkaReadType.KEY, offsetLag, -1);
    }

    public Map<Object, Object> readWithKey() {
        return consumer.read(this.topic, KafkaReadType.KEY, /* дописать получение значения из проперти kafka.offset.lag */1, -1);
    }

    /**
     * Чтение топика кафка. Возвращает значение с id (0, 1, 2, 3 ...) вместо ключа
     *
     * @param offsetLag - количество оффсетов, на которое необходимо откатить счетчик назад
     * @return - пул сообщений Map<Object, Object>
     */
    public Map<Object, Object> readWithObject(final int offsetLag) {
        return consumer.read(this.topic, KafkaReadType.OBJECT, offsetLag, -1);
    }

    public Map<Object, Object> readWithObject() {
        return consumer.read(this.topic, KafkaReadType.OBJECT, /* дописать получение значения из проперти kafka.offset.lag */1, -1);
    }

    /**
     * Проверка сообщений на содержание оргумента
     *
     * @param messages - пул сообщений HashMap<Object, Object>
     * @param args - перечесление аргументов в виде new String[] {str, str, ...}
     */
    public void check(Map<Object, Object> messages, String... args) {
        consumer.check(messages, args);
    }

    /**
     * Установить топик
     *
     * @param topic - топик
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Получить название топика
     *
     * @return - текущий топик
     */
    public String getTopic() {
        return this.topic;
    }

    public void subscribe(List<String> topics) {
        consumer.subscribe(topics);
    }

    public void subscribe(String topic) {
        subscribe(Collections.singletonList(topic));
    }

    public static class Builder {

        private final Properties producerProperties;
        private final Properties consumerProperties;

        public Builder() {
            this.producerProperties = readProperties("producer.");
            this.consumerProperties = readProperties("consumer.");
        }

        public Builder withBootstrapServer(String bootstrapServer) {
            producerProperties.setProperty("bootstrap.server", bootstrapServer);
            consumerProperties.setProperty("bootstrap.server", bootstrapServer);
            return this;
        }

        public Builder withKeySerializer(Class<? extends Serializer<?>> clazz) {
            producerProperties.setProperty("key.serializer", clazz.getCanonicalName());
            return this;
        }

        public Builder withValueSerializer(Class<? extends Serializer<?>> clazz) {
            producerProperties.setProperty("value.serializer", clazz.getCanonicalName());
            return this;
        }

        public Builder withKeyDeserializer(Class<? extends Deserializer<?>> clazz) {
            consumerProperties.setProperty("key.deserializer", clazz.getCanonicalName());
            return this;
        }

        public Builder withValueDeserializer(Class<? extends Deserializer<?>> clazz) {
            consumerProperties.setProperty("value.deserializer", clazz.getCanonicalName());
            return this;
        }

        public Builder withGroupId(String groupId) {
            consumerProperties.setProperty("groupId", groupId);
            return this;
        }

        public Builder withClientId(String clientId) {
            consumerProperties.setProperty("clientId", clientId);
            return this;
        }

        public Kafka build() {
            return new Kafka(producerProperties, consumerProperties);
        }
    }

    private static Properties readProperties(String s) {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("src/java/resources/autotest.properties")) {
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
