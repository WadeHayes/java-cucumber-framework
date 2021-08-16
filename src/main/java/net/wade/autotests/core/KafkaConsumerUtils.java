package net.wade.autotests.core;

import io.cucumber.java.it.Ma;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.time.Duration;
import java.util.*;
import java.util.Properties;
import java.util.stream.Collectors;

public class KafkaConsumerUtils {

    private static final Logger logger = LogManager.getLogger(KafkaConsumerUtils.class);

    private final Properties properties;

    public KafkaConsumerUtils(Properties properties) {
        this.properties = properties;
    }

    public void subscribe(List<String> topics) {
        KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(topics);
        consumer.close();
    }

    private List<TopicPartition> getAllPartitions(final String topic, final KafkaConsumer<Object, Object> kafkaConsumer) {
        final List<PartitionInfo> partitions = kafkaConsumer.partitionsFor(topic);
        return partitions
                .stream()
                .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void seekAllPartitionsForOffset(final Map<TopicPartition, Long> lastOffsetForAllPartitions,
                                            final KafkaConsumer<Object, Object> kafkaConsumer,
                                            final int offset) {
        lastOffsetForAllPartitions.forEach((partition, offsetPartition) -> kafkaConsumer.seek(partition, offset));
    }

    private void seekAllPartitionsForLastOffsets(final Map<TopicPartition, Long> lastOffsetForAllPartitions,
                                            final Map<TopicPartition, Long> beginningOffsets,
                                            final KafkaConsumer<Object, Object> kafkaConsumer,
                                            final int offsetLag) {
        lastOffsetForAllPartitions
                .forEach((partition, offset) -> kafkaConsumer.seek(partition,
                        (
                            (offset - offsetLag) > (beginningOffsets.containsKey(partition) ? beginningOffsets.get(partition) : 0)
                                ? (offset - offsetLag)
                                : (beginningOffsets.containsKey(partition) ? beginningOffsets.get(partition) : 0)
                        )
                    )
                );
    }

    public Map<Object, Object> read(String topic, KafkaReadType type, int offsetLag, int offset) {
        Map<Object, Object> messagePool = new HashMap<>();
        logger.info(String.format("Производим чтение с кафка топика '%s'", topic));

        KafkaConsumer<Object, Object> consumer = new KafkaConsumer<>(properties);

        List<TopicPartition> partitions = getAllPartitions(topic, consumer);
        consumer.assign(partitions);

        logger.info(String.format("Подписываемся на топик: '%s'", topic));

        Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(partitions);
        logger.info(String.format("Первые оффсеты топиков: '%s'", beginningOffsets));
        Map<TopicPartition, Long> lastOffsetForAllPartitions = consumer.endOffsets(partitions);
        logger.info(String.format("Крайние оффсеты топиков: '%s'", lastOffsetForAllPartitions));

        if (offset == -1) {
            seekAllPartitionsForLastOffsets(lastOffsetForAllPartitions, beginningOffsets, consumer, offsetLag);
        } else  {
            seekAllPartitionsForOffset(lastOffsetForAllPartitions, consumer, offset);
        }

        int iterator = 0;
        ConsumerRecords<Object, Object> consumerRecords = consumer.poll(Duration.ofMillis(/* дописать получение параметраа kafka.consumer.poll.duration из пропертей */1));
        for (ConsumerRecord<Object, Object> record : consumerRecords) {
            if (record == null) {
                continue;
            }

            if (offset == -1) {
                switch (type) {
                    case KEY:
                        Object key = (record.key() == null || messagePool.containsKey(record.key()))
                                ? (Object) record.offset()
                                : record.key();
                        messagePool.put(key, record.value());
                        break;
                    case ITERATOR:
                        messagePool.put(iterator, record.value());
                        break;
                    case OBJECT:
                        messagePool.put(iterator, record);
                        break;
                }
            }

            iterator++;
        }

        consumer.close();

        return messagePool;
    }

    public void check(Map<Object, Object> messages, String... args) {
        logger.info("Проверка сообщений кафка на содержание аргументов");
        for (String arg : args) {
            logger.trace("Check whether message contains particular property\n");
            Assert.assertTrue(messages.toString().contains(arg));
        }
    }
}
