package hr.fer.tel.rassus.stupidudp.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa {@code SimulatedDatagramSocket} proširuje klasu {@link DatagramSocket}
 * i simulira mrežno ponašanje s gubitkom paketa, kašnjenjem, jitterom i
 * kašnjenjem slanja paketa.
 *
 * <p>Ova klasa omogućuje testiranje aplikacija koje koriste UDP protokol u
 * uvjetima koji simuliraju mrežne probleme. Simulacija uključuje nasumično
 * gubljenje paketa i uvođenje kašnjenja prijenosa.</p>
 *
 * <p>Primjer upotrebe:</p>
 * <pre>
 *     double lossRate = 0.1; // 10% gubitka paketa
 *     int sendingDelay = 50; // Kašnjenje slanja od 50 ms
 *     int averageDelay = 100; // Prosječno kašnjenje od 100 ms
 *     int jitter = 20; // Varijacija kašnjenja od ±20 ms
 *
 *     SimulatedDatagramSocket socket = new SimulatedDatagramSocket(lossRate, sendingDelay, averageDelay, jitter);
 *     DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
 *     socket.send(packet);
 * </pre>
 *
 *
 * @author  Krešimir Pripužić <kresimir.pripuzic@fer.hr>
 * @see     DatagramSocket
 * @see     DatagramPacket
 * @see     java.nio.channels.DatagramChannel
 */
public class SimulatedDatagramSocket extends DatagramSocket {

    /**
     * Omjer izgubljenih paketa u simuliranoj mreži.
     * Vrijednost mora biti između 0.0 (nema gubitka) i 1.0 (svi paketi se gube).
     */
    private double lossRate;
    /**
     * Prosječno kašnjenje prijenosa paketa u milisekundama, uključujući kašnjenje slanja.
     */
    private int averageDelay;
    /**
     * Varijacija kašnjenja (jitter) paketa u milisekundama.
     */
    private int jitter;
    /**
     * Kašnjenje slanja paketa u milisekundama.
     */
    private int sendingDelay;
    /**
     * Generator slučajnih brojeva za simulaciju gubitka i kašnjenja paketa.
     */
    private Random random;
    /**
     * Akumulirano kašnjenje slanja paketa.
     */
    private int cumulatedSendingDelay;

    /**
     *
     * Kreira datagramski socket za slanje paketa putem simulirane mreže.
     *
     * <p>Postavlja parametre za simulaciju mreže, uključujući omjer izgubljenih
     * paketa, kašnjenje slanja, prosječno kašnjenje i jitter. Timeout za
     * čekanje odgovora postavlja se na dvostruku vrijednost od
     * {@code jitter + averageDelay}.</p>
     *
     *
     * Constructs a datagram socket for sending datagram packets over a
     * simulated network and binds it to any available port
     * on the local host machine.  The socket will be bound to the
     * {@link InetAddress#isAnyLocalAddress wildcard} address,
     * an IP address chosen by the kernel. A network is not simulated
     * for receiving of packets. Additional <code>SimulatedDatagramSocket</code>
     * should be used on sender side to simulate the network while receiving.
     *
     * <p>If there is a security manager,
     * its <code>checkListen</code> method is first called
     * with 0 as its argument to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     *
     * @see DatagramSocket#DatagramSocket()
     * @see SecurityManager#checkListen
     *
     * @param lossRate Packet loss ratio of the simulated network.
     * @param sendingDelay Sending delay of packets in milliseconds.
     * @param averageDelay Average delay of packets in milliseconds, including sending delay.
     * @param jitter Average delay variation of packets in milliseconds.
     *
     * @throws SocketException  if the socket could not be opened,
     *               or the socket could not bind to the specified local port.
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkListen</code> method doesn't allow the operation.
     * @throws  IllegalArgumentException if <code>sendingDelay</code> or
     * <code>averageDelay</code> is less or equal to zero.
     */
    public SimulatedDatagramSocket(double lossRate, int sendingDelay, int averageDelay, int jitter) throws SocketException, IllegalArgumentException {
        random = new Random();

        if (sendingDelay <= 0 || averageDelay <= 0) {
            throw new IllegalArgumentException("Delays should be greater than zero");
        }

        this.lossRate = lossRate;
        this.sendingDelay = sendingDelay;
        this.cumulatedSendingDelay = -sendingDelay;
        this.averageDelay = averageDelay;
        this.jitter = jitter;

        //set time to wait for answer
        super.setSoTimeout(2 * (jitter + averageDelay));
    }

    /**
     *
     * Šalje UDP paket preko simulirane mreže s primjenom simulacije gubitka, kašnjenja i jittera.
     * <p>Paket se gubi s vjerojatnošću određeno parametrom {@code lossRate}.
     * Ako se paket ne izgubi, primjenjuje se kašnjenje prijenosa i jitter
     * prije stvarnog slanja paketa.</p>
     *
     *
     * Sends a datagram packet from this socket over the simulated network.
     * The <code>DatagramPacket</code> includes information indicating the
     * data to be sent, its length, the IP address of the remote host,
     * and the port number on the remote host.
     *
     * <p>If there is a security manager, and the socket is not currently
     * connected to a remote address, this method first performs some
     * security checks. First, if <code>p.getAddress().isMulticastAddress()</code>
     * is true, this method calls the
     * security manager's <code>checkMulticast</code> method
     * with <code>p.getAddress()</code> as its argument.
     * If the evaluation of that expression is false,
     * this method instead calls the security manager's
     * <code>checkConnect</code> method with arguments
     * <code>p.getAddress().getHostAddress()</code> and
     * <code>p.getPort()</code>. Each call to a security manager method
     * could result in a SecurityException if the operation is not allowed.
     *
     * @see        DatagramSocket#send
     * @see        DatagramPacket
     * @see        SecurityManager#checkMulticast(InetAddress)
     * @see        SecurityManager#checkConnect

     * @param packet the <code>DatagramPacket</code> to be sent.
     * @throws IOException  if an I/O error occurs.
     * @throws  SecurityException  if a security manager exists and its
     *             <code>checkMulticast</code> or <code>checkConnect</code>
     *             method doesn't allow the send.
     * @throws  java.nio.channels.IllegalBlockingModeException
     *             if this socket has an associated channel,
     *             and the channel is in non-blocking mode.
     * @throws  IllegalArgumentException if the socket is connected,
     *             and connected address and packet address differ.
     */
    @Override
    public void send(DatagramPacket packet) throws IOException {
        if (random.nextDouble() >= lossRate) {
            //jitter is uniformly distributed
            cumulatedSendingDelay += sendingDelay;
            new Thread(new OutgoingDatagramPacket(packet, averageDelay - sendingDelay + cumulatedSendingDelay + (long) (2 * (random.nextDouble() - 0.5) * jitter))).start();
        }
    }

    /**
     * Interna klasa za simulaciju prijenosa paketa s kašnjenjem.
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
         * @param time ukupno vrijeme kašnjenja u milisekundama
         */
        private OutgoingDatagramPacket(DatagramPacket packet, long time) {
            this.packet = packet;
            this.time = time;
        }

        /**
         * Izvršava simulaciju kašnjenja i šalje paket.
         */
        @Override
        public void run() {
            try {
                //simulate sending delay
                Thread.sleep(sendingDelay);
                cumulatedSendingDelay -= sendingDelay;

                //simulate network delay
                Thread.sleep(time);
                SimulatedDatagramSocket.super.send(packet);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } catch (IOException ex) {
                Logger.getLogger(SimulatedDatagramSocket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
