/*
 * This code has been developed at Departement of Telecommunications,
 * Faculty of Electrical Engineering and Computing, University of Zagreb.
 */
package hr.fer.tel.rassus.stupidudp.server;

import hr.fer.tel.rassus.stupidudp.network.SimpleSimulatedDatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Klasa {@code StupidUDPServer} predstavlja jednostavan UDP server koji prima
 * poruke od klijenta, obrađuje ih tako da ih pretvori u velika slova,
 * te vraća obrađene poruke natrag klijentu.
 *
 * <p>Server koristi UDP protokol za komunikaciju putem mreže. Za simulaciju
 * mrežnih uvjeta koristi se klasa {@link SimpleSimulatedDatagramSocket},
 * koja omogućuje simulaciju gubitka paketa i kašnjenja u mreži.</p>
 *
 * <p>Primjer komunikacije između klijenta i servera:</p>
 * <pre>
 * Klijent šalje: "hello"
 * Server prima: "hello"
 * Server šalje: "HELLO"
 * </pre>
 *
 * <p>Server kontinuirano čeka dolazne zahtjeve, obrađuje ih i odgovara dok
 * se ne prekine ručno.</p>
 *
 *
 * @author Krešimir Pripužić <kresimir.pripuzic@fer.hr>
 */
public class StupidUDPServer {
    /**
     * Port na kojem server osluškuje dolazne zahtjeve.
     */
    static final int PORT = 10001; // server port

    /**
     /**
     * Ulazna točka aplikacije.
     *
     * <p>Glavna metoda stvara UDP socket na specificiranom portu koristeći
     * klasu {@link SimpleSimulatedDatagramSocket}. Server prima dolazne
     * poruke, pretvara ih u velika slova i vraća kao odgovor.</p>
     *
     * @param args argumenti komandne linije (ne koriste se)
     * @throws IOException ako dođe do pogreške prilikom slanja ili primanja paketa

     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {

        byte[] rcvBuf = new byte[256]; // Buffer za primljene podatke
        byte[] sendBuf = new byte[256];// Buffer za slanje podataka
        String rcvStr;

        // Kreira UDP socket s parametrima za simulaciju mreže
        // create a UDP socket and bind it to the specified port on the local
        // host
        DatagramSocket socket = new SimpleSimulatedDatagramSocket(PORT, 0.2, 200); //SOCKET -> BIND

        // Glavna petlja za obradu zahtjeva
        while (true) { //OBRADA ZAHTJEVA
            // Kreira DatagramPacket za primanje podataka
            DatagramPacket packet = new DatagramPacket(rcvBuf, rcvBuf.length);

            // Prima paket od klijenta
            socket.receive(packet); //RECVFROM

            // Dekodira primljeni niz bajtova u String
            // construct a new String by decoding the specified subarray of bytes using the platform's default charset
            rcvStr = new String(packet.getData(), packet.getOffset(),
                    packet.getLength());
            System.out.println("Server received: " + rcvStr);

            // Pretvara primljeni String u velika slova
            // encode a String into a sequence of bytes using the platform's default charset
            sendBuf = rcvStr.toUpperCase().getBytes();
            System.out.println("Server sends: " + rcvStr.toUpperCase());

            // Stvara DatagramPacket za slanje odgovora
            // create a DatagramPacket for sending packets
            DatagramPacket sendPacket = new DatagramPacket(sendBuf,
                    sendBuf.length, packet.getAddress(), packet.getPort());

            // Šalje odgovor klijentu
            // send packet
            socket.send(sendPacket); //SENDTO
        }
    }
}
