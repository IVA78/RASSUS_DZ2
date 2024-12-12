/*
 * This code has been developed at Departement of Telecommunications,
 * Faculty of Electrical Engineering and Computing, University of Zagreb.
 */
package hr.fer.tel.rassus.stupidudp.server;

import hr.fer.tel.rassus.Sensor;
import hr.fer.tel.rassus.stupidudp.network.SimpleSimulatedDatagramSocket;
import hr.fer.tel.rassus.utils.ReadingDTO;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;

/**
 * ULOGA UDP POSLUZITELJA
 *
 * 1. Primanje podataka od drugih čvorova:
 *      - Poslužitelj sluša dolazne UDP poruke na definiranom portu.
 *      - Prima podatke (očitavanja senzora i vremenske oznake) koje mu šalju drugi čvorovi u mreži.
 * 2. Odgovaranje na zahtjeve:
 *      - slanje ACK
 */

public class StupidUDPServer {
    private SimpleSimulatedDatagramSocket socket;
    private int port;

    public StupidUDPServer(int port, double lossRate, int averageDelay) throws Exception {
        this.socket = new SimpleSimulatedDatagramSocket(port, lossRate, averageDelay); //BIND
        this.port = port;
    }

    public void startServer() throws IOException {
        String msg;
        byte[] rcvBuf = new byte[1024]; // Buffer za primanje datagrama - SOCKET
        byte[] sendBuffAck; // Buffer za slanje datagrama

        while (!Sensor.getStop()) {
            try {
                boolean repeated = false;

                // primanje datagrama
                DatagramPacket packet = new DatagramPacket(rcvBuf, rcvBuf.length);
                socket.receive(packet); // RECVFROM

                // izvlacenje podataka iz datagrama
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

                ReadingDTO readingDTO = (ReadingDTO) SerializationUtils.deserialize(data);
                System.out.println("Server " + Sensor.getSensorId() +"| received: " + readingDTO);

                // provjera za duplicirane datagrame
                List<ReadingDTO> readingDTOList = Sensor.getNeighboursReadingDTOList();
                for (ReadingDTO readingDTOFromList : readingDTOList) {
                    if (readingDTO.equals(readingDTOFromList)) {
                        repeated = true;
                        break;
                    }
                }

                // priprema ACK poruke
                if (!repeated) {
                    msg = "ACK from sensor with ID: " + Sensor.getSensorId();
                    readingDTOList.add(readingDTO);
                    Sensor.setNeighboursReadingDTOList(readingDTOList);
                    Sensor.setVectorTime(Sensor.getVectorTime() + 1);

                    //vector time update??

                } else {
                    msg = "ACK (REPEATED) for sensor with ID: " + readingDTO.getSensorId();
                }

                System.out.println("Server " + Sensor.getSensorId() +"| sends: " + msg);

                // slanje ACK
                sendBuffAck = msg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffAck, sendBuffAck.length, packet.getAddress(), packet.getPort());
                socket.send(sendPacket); // SENDTO

            } catch (ClassCastException e) {

                System.err.println("Server " + Sensor.getSensorId() +"| Error deserializing received data: " + e.getMessage());
                //e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Server " + Sensor.getSensorId() +"| Network error while receiving/sending packets: " + e.getMessage());
                //e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Server " + Sensor.getSensorId() +"| Unexpected server error: " + e.getMessage());
                //e.printStackTrace();
            }
        }
        stopServer();
    }

    public void stopServer() {
        socket.close();  // Close the socket to stop receiving packets
        System.out.println("Server " + Sensor.getSensorId() + " stopped.");
    }

}
