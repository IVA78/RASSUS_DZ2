package hr.fer.tel.rassus;

import hr.fer.tel.rassus.stupidudp.client.StupidUDPClient;
import hr.fer.tel.rassus.stupidudp.network.EmulatedSystemClock;
import hr.fer.tel.rassus.stupidudp.server.StupidUDPServer;
import hr.fer.tel.rassus.utils.ReadingDTO;
import hr.fer.tel.rassus.utils.Utils;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Sensor {
    private static Long startTime = 0L;
    private static Integer sensorId;
    private static Integer vectorTime = 0;

    private static List<ReadingDTO> neighboursReadingDTOList = new ArrayList<>();
    private static List<ReadingDTO> myReadingDTOList = new ArrayList<>();
    public static long getStartTime() {
        return startTime;
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

    public static void main(String[] args) throws Exception {
        //biljezenje pocetka rada aplikacije
        EmulatedSystemClock emulatedSystemClock = new EmulatedSystemClock();

        //trazenje upisa identifikatora i UDP porta
        Integer id = Utils.generateRandomInteger(0, 100);
        Sensor.setSensorId(id);
        String adress = "localhost";
        Integer serverPort = Utils.generateRandomInteger(8000, 65000);
        Integer clientPort = Utils.generateRandomInteger(8000, 65000);
        while (clientPort.equals(serverPort)) { // Osigurajte da se ne poklapa sa serverovim portom
            clientPort = Utils.generateRandomInteger(8000, 65000);
        }
        System.out.println("Server port: " + serverPort);
        System.out.println("Client port: " + clientPort);

        //pretplata na teme "Register" i "Command"

        //cekanje kontrolne poruke "Start"

        //slanje poruke na temu "Register" - registracija -> PRODUCER

        //dohvat identifikatora susjeda -> CONSUMER


        //UDP klijent i posluzitelj
        StupidUDPServer stupidUDPServer = new StupidUDPServer(serverPort,0.3, 1000);
        StupidUDPClient stupidUDPClient = new StupidUDPClient(0.3, 1000);

        // Pokretanje UDP servera u zasebnoj dretvi
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


        while(true) {
            //dohvat vlastitog ocitanja (generiranje)
            ReadingDTO readingDTO = Utils.parseReading(emulatedSystemClock);
            readingDTO.setSensorId(id);
            myReadingDTOList.add(readingDTO);
            System.out.println(readingDTO.toString());

            //slanje ocitanja od drugim senzorima
            Thread.sleep(1000); // Simulate a delay before the client requests a reading
            stupidUDPClient.sendReading( readingDTO, InetAddress.getByName(adress), serverPort);

        }



    }
}
