package hr.fer.tel.rassus.kafkaconfig;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class KafkaConfig {


    //Senzor ima ulogu Producera kada šalje poruku na temu "Register"
    public static Properties getSensorProducerProperties() {

        Properties sensorProducesProperties = new Properties();
        sensorProducesProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        sensorProducesProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        sensorProducesProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return sensorProducesProperties;
    }

    //Senzor ima ulogu Consumera kada dohvaća info o susjedima
    public static Properties getSensorConsumerProperties() {

        Properties sensorConsumerProperties = new Properties();
        sensorConsumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        sensorConsumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        sensorConsumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        sensorConsumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        sensorConsumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        sensorConsumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return sensorConsumerProperties;
    }

    //Kafka kontroler šalje kontrole poruke "Start" i "Stop" za pokretanje i zaustavljanje senzora
    public static Properties getControllerPropperties()  {

        Properties controllerProperties = new Properties();
        controllerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        controllerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        controllerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return controllerProperties;
    }

}
