package net.wade.autotests.core;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class KafkaProducerUtils {

    private static final Logger logger = LogManager.getLogger(KafkaProducerUtils.class);

    private final Properties properties;


    public KafkaProducerUtils(Properties properties) {
        this.properties = properties;
    }

    public void sendMessageToTopic(Object key, String topic, Object message) {
        logger.info("Отправляем сообщение в кафка топик");
        KafkaProducer<Object,Object> producer = new KafkaProducer<Object, Object>(properties);
        producer.send(new ProducerRecord<>(topic, key, message));
        producer.close();
    }
}
