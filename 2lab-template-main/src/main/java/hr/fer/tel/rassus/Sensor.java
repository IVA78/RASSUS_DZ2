package hr.fer.tel.rassus;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.fer.tel.rassus.kafkaconfig.KafkaConfig;
import hr.fer.tel.rassus.stupidudp.client.StupidUDPClient;
import hr.fer.tel.rassus.stupidudp.network.EmulatedSystemClock;
import hr.fer.tel.rassus.stupidudp.server.StupidUDPServer;
import hr.fer.tel.rassus.utils.ReadingDTO;
import hr.fer.tel.rassus.utils.SensorData;
import hr.fer.tel.rassus.utils.Utils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.swing.*;
import java.net.InetAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Sensor {
    private static Long startTime = 0L;
    private static Integer sensorId;
    private static Integer vectorTime = 0;

    private static List<ReadingDTO> neighboursReadingDTOList = new ArrayList<>();
    private static List<ReadingDTO> myReadingDTOList = new ArrayList<>();
    private static List<ReadingDTO> mySentReadingDTOList = new ArrayList<>();
    public static long getStartTime() {
        return startTime;
    }

    private volatile static  Boolean start = false;
    private volatile  static Boolean stop = false;
    private volatile static List<SensorData> neighbourSensors = new ArrayList<>();

    public static Boolean getStart() {
        return start;
    }

    public static void setStart(Boolean start) {
        Sensor.start = start;
    }

    public static Boolean getStop() {
        return stop;
    }

    public static void setStop(Boolean stop) {
        Sensor.stop = stop;
    }

    public static Integer getSensorId() {
        return sensorId;
    }

    public static void setSensorId(Integer sensorId) {
        Sensor.sensorId = sensorId;
    }

    public static Integer getVectorTime() {
        return vectorTime;
    }
    public static void setVectorTime(Integer time) {
        vectorTime = time;
    }

    public static List<ReadingDTO> getNeighboursReadingDTOList() {
        return neighboursReadingDTOList;
    }

    public static void setNeighboursReadingDTOList(List<ReadingDTO> neighboursReadingDTOList) {
        Sensor.neighboursReadingDTOList = neighboursReadingDTOList;
    }

    public static List<ReadingDTO> getMyReadingDTOList() {
        return myReadingDTOList;
    }

    public static void setMyReadingDTOList(List<ReadingDTO> myReadingDTOList) {
        Sensor.myReadingDTOList = myReadingDTOList;
    }

    public static List<SensorData> getNeighbourSensors() {
        return neighbourSensors;
    }

    public static void setNeighbourSensors(List<SensorData> neighbourSensors) {
        Sensor.neighbourSensors = neighbourSensors;
    }

    public static List<ReadingDTO> getMySentReadingDTOList() {
        return mySentReadingDTOList;
    }

    public static void setMySentReadingDTOList(List<ReadingDTO> mySentReadingDTOList) {
        Sensor.mySentReadingDTOList = mySentReadingDTOList;
    }

    public static void main(String[] args) throws Exception {
        //biljezenje pocetka rada aplikacije
        EmulatedSystemClock emulatedSystemClock = new EmulatedSystemClock();

        //trazenje upisa identifikatora i UDP porta
        Integer id = Utils.generateRandomInteger(0, 100);
        Sensor.setSensorId(id);
        String adress = "localhost";
        Integer serverPort = Utils.generateRandomInteger(8000, 65000);
        System.out.println("Server port: " + serverPort);
        startTime = emulatedSystemClock.currentTimeMillis(); //azuriranje pocetnog vremena

        //generiranje JSON podataka o senzoru
        String generatedJSON = Utils.generateJsonData(sensorId, adress, serverPort);

        //slanje poruke na temu "Register" - registracija -> PRODUCER
        Producer<String, String> sensorProducer = new KafkaProducer<>(KafkaConfig.getSensorProducerProperties());
        ProducerRecord<String, String> record = new ProducerRecord<>("Register", null, generatedJSON);
        sensorProducer.send(record);
        sensorProducer.flush();

        //pretplata na temu "Register" i "Command" -> CONSUMER
        Consumer<String, String> sensorConsumerRegister = new KafkaConsumer<>(KafkaConfig.getSensorConsumerRegisterProperties());
        sensorConsumerRegister.subscribe(Collections.singleton("Register"));
        Consumer<String, String> sensorConsumerCommand = new KafkaConsumer<>(KafkaConfig.getSensorConsumerCommandProperties());
        sensorConsumerCommand.subscribe(Collections.singleton("Command"));
        Thread.sleep(3000);



        //cekanje kontrolne poruke "Start" - dohvat susjeda -> CONSUMER
        while (!start) {

            Thread.sleep(3000);

            // Assign partitions (if not already done by Kafka internally)
            sensorConsumerRegister.assignment().forEach(topicPartition -> {
                sensorConsumerRegister.seekToBeginning(Collections.singleton(topicPartition));
            });

            // dohvat poruka sa kafke
            ConsumerRecords<String, String> registrationRecords = sensorConsumerRegister.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> registrationRecord : registrationRecords) {
                String jsonValue = registrationRecord.value(); //Dohvat poruke iz zapisa
                //System.out.println("Raw JSON Value: " + jsonValue);

                try {
                    // Parsiranje JSON-a u SensorData objekt
                    SensorData neighbourSensorData = Utils.parseJson(jsonValue);
                    //System.out.println("Parsed Neighbour Sensor Data: " + neighbourSensorData);
                    if(!neighbourSensorData.getId().equals(getSensorId()) ) { //nije rijec o trenutnom senzoru
                        Boolean exists = false;
                        for (SensorData existingSensor : Sensor.getNeighbourSensors()) {
                            if (existingSensor.getId().equals(neighbourSensorData.getId())) {
                                exists =  true; // senzor vec postoji u listi
                            }
                        }
                        if(!exists){
                            neighbourSensors.add(neighbourSensorData);
                        }

                    }

                } catch (Exception e) {
                    System.err.println("Failed to parse JSON: " + e.getMessage());
                }
            }
            System.out.println("Sensor id: " + getSensorId());
            for(SensorData neighbour : neighbourSensors) {
                System.out.println("Neighbour: " + neighbour);
            }

            //provjera je li stigla poruka "Start"
            sensorConsumerCommand.assignment().forEach(topicPartition -> {
                sensorConsumerCommand.seekToBeginning(Collections.singleton(topicPartition));
            });

            ConsumerRecord<String, String> lastRecord = null;

            while (lastRecord == null) {
                ConsumerRecords<String, String> records = sensorConsumerCommand.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> commandRecord : records) {
                    lastRecord = commandRecord;
                }

                if(records.isEmpty()){
                    break;
                }
            }

            if(lastRecord != null && lastRecord.value().equals("Start")) {
                start = true;
            }

        }




        // Pokretanje UDP servera u zasebnoj dretvi
        StupidUDPServer stupidUDPServer = new StupidUDPServer(serverPort,0.3, 1000);
        Thread serverThread = new Thread(() -> {
            try {
                stupidUDPServer.startServer();
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();


        //sortitanje i izracunavanje srednje vrijednosti u vremenskom prozoru od 5 sekundi
        //dretva ?




        //GLAVNI DIO - funkcionalnost senzora
        StupidUDPClient stupidUDPClient = new StupidUDPClient(0.3, 1000);

        while(!stop) {
            //dohvat vlastitog ocitanja (generiranje)
            ReadingDTO readingDTO = Utils.parseReading(emulatedSystemClock);
            readingDTO.setSensorId(id);
            myReadingDTOList.add(readingDTO);
            System.out.println("My reading (" + getSensorId() + "): "  + readingDTO.toString());

            //azuriranje vektorske vremenske oznake pri generiranju ocitanja -> UNUTARNJI DOGADJAJ
            vectorTime = vectorTime + 1;

            //slanje ocitanja od drugim senzorima -> VANJSKI DOGADJAJ (povecavamo samo 1x jer je retransmisija prakticki "isti dogadjaj")
            vectorTime = vectorTime + 1;
            stupidUDPClient.sendReading(readingDTO);
            Thread.sleep(1000);

            //provjera je li stigla "Stop poruka"
            sensorConsumerCommand.assignment().forEach(topicPartition -> {
                sensorConsumerCommand.seekToBeginning(Collections.singleton(topicPartition));
            });

            ConsumerRecord<String, String> lastRecord = null;

            while (lastRecord == null) {
                ConsumerRecords<String, String> records = sensorConsumerCommand.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> commandRecord : records) {
                    lastRecord = commandRecord;
                }
            }

            if(lastRecord != null && lastRecord.value().equals("Stop")) {
                stop = true;
            }

        }

        System.out.println("---------------------------------------------------------------------------");

        //ispis svega ocitanog
        System.out.println("Sensor " + getSensorId() + "| Ispis svega ocitanog");
        for(ReadingDTO myReading : getMyReadingDTOList()) {
            System.out.println(myReading);
        }

        System.out.println("---------------------------------------------------------------------------");

        //ispis svega poslanog
        System.out.println("Sensor " + getSensorId() + "| Ispis svega poslanog");
        for(ReadingDTO myReading : getMySentReadingDTOList()) {
            System.out.println(myReading);
        }

        System.out.println("---------------------------------------------------------------------------");


        //ispis svega primljenog
        System.out.println("Sensor " + getSensorId() + "| Ispis svega primljenog");
        for(ReadingDTO neighbourReading : getNeighboursReadingDTOList()) {
            System.out.println(neighbourReading);
        }


    }

}
