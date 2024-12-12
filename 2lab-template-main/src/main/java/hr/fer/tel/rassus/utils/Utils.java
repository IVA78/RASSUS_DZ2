package hr.fer.tel.rassus.utils;

import hr.fer.tel.rassus.Sensor;
import hr.fer.tel.rassus.stupidudp.network.EmulatedSystemClock;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Utils {



    public static ReadingDTO parseReading(EmulatedSystemClock emulatedSystemClock) {

        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("C:\\Users\\38595\\Documents\\GitHub\\RASSUS_DZ1\\client\\readings.csv"));
        } catch (IOException e) {
            System.out.println("Failed to read CSV");
        }

        //dohvat nasumicno odabrane linije za ocitanje
        Long currentTime = emulatedSystemClock.currentTimeMillis();
        Long sensorStartTime = Sensor.getStartTime();
        Long red = ((currentTime - sensorStartTime) / 1000) % 100+1;
        String randomLine = lines.get(red.intValue());

        int cnt = 0;

        //parsiranje linije za ocitanje
        HashMap<String, String> readingValues = new HashMap<>();
        randomLine = randomLine + "$,";
        for (String s : randomLine.split(",")) {
            switch (cnt) {
                case 0:
                    readingValues.put("temperature", s == "" ? "" : s.trim());
                    break;
                case 1:
                    readingValues.put("pressure", s == "" ? "" : s.trim());
                    break;
                case 2:
                    readingValues.put("humidity", s == "" ? "" : s.trim());
                    break;
                case 3:
                    readingValues.put("co", s == "" ? "" : s.trim());
                    break;
                case 4:
                    readingValues.put("no2", s == "" ? "" : s.trim());
                    break;
                case 5:
                    readingValues.put("so2", s == "" ? "" : s.trim());
                    break;
                default:
                    break;
            }
            cnt++;
        }

        Integer temperature = Integer.parseInt(readingValues.get("temperature"));
        Integer pressure = Integer.parseInt(readingValues.get("pressure"));
        Integer humidity = Integer.parseInt(readingValues.get("humidity"));
        Integer co = readingValues.get("co") == "" ? 0 : Integer.parseInt(readingValues.get("co"));
        Integer no2 = readingValues.get("no2") == "" ? 0 : Integer.parseInt(readingValues.get("no2"));
        Integer so2 = readingValues.get("so2") == "" ? 0 : Integer.parseInt(readingValues.get("so2"));

        //azuriranje vektorske oznake vremena senzora
        Sensor.setVectorTime(Sensor.getVectorTime() + 1);

        //dodjeljivanje vrijednosti objektu za ocitanje
        ReadingDTO readingDTO = new ReadingDTO();
        readingDTO.setNo2(no2);
        readingDTO.setScalarTime(currentTime);
        readingDTO.setVectorTime(Sensor.getVectorTime());


        return readingDTO;

    }

    public static Integer generateRandomInteger(Integer min, Integer max) {
        Random random = new Random();
        return min + random.nextInt(max - min);
    }

    // Helper method to serialize ReadingDTO to byte array
    public static byte[] serializeReadingDTO(ReadingDTO reading) throws Exception {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(reading);
            return byteStream.toByteArray();
        }
    }

    // Helper method to deserialize byte array into ReadingDTO
    public static  ReadingDTO deserializeReadingDTO(byte[] data) throws Exception {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            return (ReadingDTO) objectStream.readObject();
        }
    }

}
