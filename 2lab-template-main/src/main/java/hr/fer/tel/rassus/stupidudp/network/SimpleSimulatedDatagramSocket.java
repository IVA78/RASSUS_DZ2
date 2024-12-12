package hr.fer.tel.rassus.stupidudp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa  SimpleSimulatedDatagramSocket proširuje klasu DatagramSocket
 * i simulira ponašanje mreže s gubitkom paketa i kašnjenjem.
 *
 * <p>Ova klasa omogućuje testiranje aplikacija koje koriste UDP protokol u uvjetima
 * koji uključuju slučajni gubitak paketa i kašnjenje prijenosa. Gubitak paketa i
 * kašnjenje definirani su parametrima prilikom stvaranja instance.</p>
 *
 * <p>Primjer upotrebe (za klijentsku stranu):</p>
 * <pre>
 *     double lossRate = 0.1; // 10% gubitka paketa
 *     int averageDelay = 100; // prosječno kašnjenje od 100 ms
 *     SimpleSimulatedDatagramSocket socket = new SimpleSimulatedDatagramSocket(lossRate, averageDelay);
 *     DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
 *     socket.send(packet);
 * </pre>
 *
 */

public class SimpleSimulatedDatagramSocket extends DatagramSocket {

    /**
     * Postotak gubitka paketa. Vrijednost mora biti u rasponu [0.0, 1.0],
     * gdje 0.0 znači da nema gubitka, a 1.0 znači da se svi paketi gube.
     */
    private final double lossRate;
    /**
     * Prosječno kašnjenje prijenosa paketa u milisekundama.
     * Kašnjenje je nasumično odabrano u rasponu od 0 do 2 * {@code averageDelay}.
     */
    private final int averageDelay;
    /**
     * Generator slučajnih brojeva za simulaciju gubitka paketa i kašnjenja.
     */
    private final Random random;

    /**
     * Konstruktor za serversku stranu.
     * <p>Postavlja port za socket, postotak gubitka paketa i prosječno
     * kašnjenje, bez definiranja timeouta za čekanje odgovora.</p>
     *
     * @param port port na kojem socket sluša
     * @param lossRate postotak gubitka paketa (u rasponu [0.0, 1.0])
     * @param averageDelay prosječno kašnjenje u milisekundama
     * @throws SocketException ako se socket ne može otvoriti
     * @throws IllegalArgumentException ako su parametri neispravni
     */
    //use this constructor for the server side (no timeout)
    public SimpleSimulatedDatagramSocket(int port, double lossRate, int averageDelay) throws SocketException, IllegalArgumentException {
        super(port);
        random = new Random();

        this.lossRate = lossRate;
        this.averageDelay = averageDelay;

        //set time to wait for answer
        super.setSoTimeout(7000);
    }

    /**
     * Konstruktor za klijentsku stranu.
     * <p>Postavlja postotak gubitka paketa, prosječno kašnjenje i timeout
     * čekanja odgovora na 4 * {@code averageDelay}.</p>
     *
     * @param lossRate postotak gubitka paketa (u rasponu [0.0, 1.0])
     * @param averageDelay prosječno kašnjenje u milisekundama
     * @throws SocketException ako se socket ne može otvoriti
     * @throws IllegalArgumentException ako su parametri neispravni
     */
    //use this constructor for the client side (timeout = 4 * averageDelay)
    public SimpleSimulatedDatagramSocket(double lossRate, int averageDelay) throws SocketException, IllegalArgumentException {
        random = new Random();

        this.lossRate = lossRate;
        this.averageDelay = averageDelay;

        //set time to wait for answer
        super.setSoTimeout(4 * averageDelay);
    }

    /**
     * Šalje UDP paket s primjenom simulacije gubitka i kašnjenja paketa.
     * <p>Simulacija gubitka se temelji na slučajnom broju, gdje je paket
     * izgubljen ako je slučajni broj manji od {@code lossRate}. Ako paket
     * nije izgubljen, dodaje se nasumično kašnjenje prije slanja paketa.</p>
     *
     * @param packet paket za slanje
     * @throws IOException ako dođe do pogreške prilikom slanja paketa
     */
    @Override
    public void send(DatagramPacket packet) throws IOException {
        if (random.nextDouble() >= lossRate) {
            //delay is uniformely distributed between 0 and 2*averageDelay
            new Thread(new OutgoingDatagramPacket(packet, (long) (2 * averageDelay * random.nextDouble()))).start();
        }
    }

    /**
     * Interna klasa za simulaciju odgođenog slanja paketa.
     * Inner class for internal use.
     */
    private class OutgoingDatagramPacket implements Runnable {
        /**
         * Paket koji treba poslati.
         */
        private final DatagramPacket packet;
        /**
         * Vrijeme kašnjenja prije slanja u milisekundama.
         */
        private final long time;



        /**
         * Kreira instancu klase {@code OutgoingDatagramPacket}.
         *
         * @param packet paket koji treba poslati
         * @param time vrijeme kašnjenja u milisekundama
         */
        private OutgoingDatagramPacket(DatagramPacket packet, long time) {
            this.packet = packet;
            this.time = time;
        }

        /**
         * Izvršava slanje paketa s kašnjenjem.
         * <p>Simulira mrežno kašnjenje koristeći metodu {@code Thread.sleep}
         * i zatim šalje paket koristeći superklasinu metodu {@code send}.</p>
         */
        @Override
        public void run() {
            try {
                //simulate network delay
                Thread.sleep(time);
                SimpleSimulatedDatagramSocket.super.send(packet);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (IOException ex) {
                Logger.getLogger(SimulatedDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

