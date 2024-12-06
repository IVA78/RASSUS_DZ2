/*
 * This code has been developed at Departement of Telecommunications,
 * Faculty of Electrical Eengineering and Computing, University of Zagreb.
 */
package hr.fer.tel.rassus.stupidudp.client;

import hr.fer.tel.rassus.stupidudp.network.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa {@code StupidUDPClient} predstavlja jednostavan UDP klijent koji šalje
 * niz znakova serveru, pri čemu se svaki znak šalje kao zaseban UDP paket.
 * Nakon što server obradi podatke, klijent prima obrađene podatke natrag.
 *
 * <p>Klijent koristi UDP protokol za komunikaciju s poslužiteljem. Za simulaciju
 * mrežnih uvjeta (gubitak paketa i kašnjenje) koristi se klasa
 * {@link SimpleSimulatedDatagramSocket}.</p>
 *
 * <p>Primjer komunikacije:</p>
 * <pre>
 * Klijent šalje: "hello"
 * Server prima: "hello"
 * Server šalje: "HELLO"
 * Klijent prima: "HELLO"
 * </pre>
 *
 * @author Krešimir Pripužić <kresimir.pripuzic@fer.hr>
 */
public class StupidUDPClient {

    /**
     * Port na kojem server osluškuje dolazne zahtjeve.
     */
    static final int PORT = 10001; // server port

    /**
     * Ulazna točka aplikacije.
     *
     * <p>Glavna metoda klijenta stvara UDP socket za komunikaciju sa serverom.
     * Klijent šalje svaki znak iz niza kao poseban UDP paket i zatim prima
     * obrađeni niz od servera. Komunikacija se odvija preko simulirane mreže
     * s parametrima za gubitak paketa i kašnjenje.</p>
     *
     * @param args argumenti komandne linije (ne koriste se)
     * @throws IOException ako dođe do pogreške prilikom slanja ili primanja podataka
     */
    public static void main(String args[]) throws IOException {

        // Inicijalizacija stringa koji se šalje
        String sendString = "Any string...";

        // Buffer za primanje podataka
        byte[] rcvBuf = new byte[256]; // received bytes

        // encode this String into a sequence of bytes using the platform's
        // default charset and store it into a new byte array

        // IP adresa servera (localhost)
        // determine the IP address of a host, given the host's name
        InetAddress address = InetAddress.getByName("localhost");

        // Kreira UDP socket sa simuliranim mrežnim parametrima
        // create a datagram socket and bind it to any available port on the local host
        //DatagramSocket socket = new SimulatedDatagramSocket(0.2, 1, 200, 50); //SOCKET
        DatagramSocket socket = new SimpleSimulatedDatagramSocket(0.2, 200); //SOCKET

        // Slanje svakog znaka kao zaseban UDP paket
        System.out.print("Client sends: ");
        // send each character as a separate datagram packet
        for (int i = 0; i < sendString.length(); i++) {
            byte[] sendBuf = new byte[1];// Buffer za slanje
            sendBuf[0] = (byte) sendString.charAt(i);

            // Stvaranje DatagramPacket-a za slanje
            // create a datagram packet for sending data
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length,
                    address, PORT);

            // Slanje paketa
            // send a datagram packet from this socket
            socket.send(packet); //SENDTO
            System.out.print(new String(sendBuf));
        }
        System.out.println("");

        // String za spremanje primljenih podataka
        StringBuffer receiveString = new StringBuffer();

        // Primanje obrađenih podataka od servera
        while (true) {
            // create a datagram packet for receiving data
            DatagramPacket rcvPacket = new DatagramPacket(rcvBuf, rcvBuf.length);

            try {
                // Primanje paketa
                // receive a datagram packet from this socket
                socket.receive(rcvPacket); //RECVFROM
            } catch (SocketTimeoutException e) {
                // Prekid petlje ako dođe do isteka vremena
                break;
            } catch (IOException ex) {
                Logger.getLogger(StupidUDPClient.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Dodavanje primljenih podataka u rezultat
            // construct a new String by decoding the specified subarray of bytes using the platform's default charset
            receiveString.append(new String(rcvPacket.getData(), rcvPacket.getOffset(), rcvPacket.getLength()));

        }
        System.out.println("Client received: " + receiveString);

        // Zatvaranje socket-a
        // close the datagram socket
        socket.close(); //CLOSE
    }
}
