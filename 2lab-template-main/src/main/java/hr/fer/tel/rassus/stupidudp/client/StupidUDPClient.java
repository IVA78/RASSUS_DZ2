/*
 * This code has been developed at Departement of Telecommunications,
 * Faculty of Electrical Eengineering and Computing, University of Zagreb.
 */
package hr.fer.tel.rassus.stupidudp.client;

import hr.fer.tel.rassus.Sensor;
import hr.fer.tel.rassus.stupidudp.network.*;
import hr.fer.tel.rassus.utils.ReadingDTO;
import hr.fer.tel.rassus.utils.SensorData;
import hr.fer.tel.rassus.utils.Utils;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * ULOGA UDP KLIJENTA
 *
 * 0. Pokretanje komunikacije
 * 1. Slanje podataka drugim cvorovima
 * 2. Primanje ACK poruke s naglaskom na restransmisiju
 */

public class StupidUDPClient {

    private SimpleSimulatedDatagramSocket socket;

    public StupidUDPClient( double lossRate, int averageDelay) throws Exception {
        this.socket = new SimpleSimulatedDatagramSocket(lossRate, averageDelay); //SOCKET
    }
    public void sendReading(ReadingDTO readingDTO) throws IOException {

        System.out.println("Client " + Sensor.getSensorId() + "| sends: " + readingDTO + " to sensor "+ readingDTO.getSensorId());

        byte[] sendBuf = SerializationUtils.serialize(readingDTO);
        byte [] confirm = new byte[256];

        DatagramPacket packetAck = new DatagramPacket(confirm, confirm.length);

        //petlja za slanje ocitanja svim susjedima
        for(SensorData neighbour : Sensor.getNeighbourSensors()) {


            InetAddress address = InetAddress.getByName(neighbour.getAddress());
            Integer port = neighbour.getPort();

            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port);


            System.out.println("Client " + Sensor.getSensorId() + "| sending data to sensor with id " + neighbour.getId()+" on port " + neighbour.getPort());
            socket.send(packet); //SENDTO

            try {
                // Cekanje na ACK

                socket.receive(packetAck);

                String receiveString = new String(packetAck.getData(), packetAck.getOffset(), packetAck.getLength());

                String ack = "  Client " + Sensor.getSensorId() +"| Ack recevided --> " + receiveString + " from port " +  packetAck.getPort();
                System.out.println(ack);

                break;
            } catch (SocketTimeoutException e) {
                String lost = "Client " + Sensor.getSensorId() +"| I lost packet, sending again" + " to port " +  packetAck.getPort() + "\n";
                System.out.println(lost);
            } catch (Exception exception) {
                Logger.getLogger(StupidUDPClient.class.getClass().getName()).log(Level.ALL, "Something went wrong", exception);
            }
            System.out.print("\n");

        }

    }
}
