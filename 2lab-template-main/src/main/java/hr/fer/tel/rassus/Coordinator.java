package hr.fer.tel.rassus;

import hr.fer.tel.rassus.kafkaconfig.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Scanner;

public class Coordinator {

    public static void main(String[] args) {

        Producer<String, String> producer = new KafkaProducer<>(KafkaConfig.getControllerPropperties());

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Write a message to send to consumer on topic Command {Start, Stop}");
            String command = sc.nextLine();

            ProducerRecord<String, String> record = new ProducerRecord<>("Command", null, command);

            producer.send(record);
            producer.flush();

            if (command.equals("Stop"))
            {
                sc.close();
                return;
            }
        }

    }
}
