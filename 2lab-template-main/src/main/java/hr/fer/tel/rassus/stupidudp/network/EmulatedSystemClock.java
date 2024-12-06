/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package hr.fer.tel.rassus.stupidudp.network;

import java.util.Random;

/**
 * Klasa simulira sustavni sat s nasumičnim odstupanjem
 * (jitter) primijenjenim na izračun vremena. Korisna je za testiranje distribuiranih
 * sustava u kojima sinkronizacija vremena nije savršena te je potrebno simulirati
 * odstupanja među satovima.
 *
 * <p>Klasa generira nasumičan postotak odstupanja (jitter) između 0% i 20% prilikom
 * inicijalizacije. Ovo odstupanje se eksponencijalno primjenjuje tijekom vremena kako
 * bi se simulirala rastuća devijacija od stvarnog sustavnog sata.</p>
 *
 * <p>Primjer upotrebe:</p>
 * <pre>
 *     EmulatedSystemClock sat = new EmulatedSystemClock();
 *     long emuliranoVrijeme = sat.currentTimeMillis();
 *     System.out.println("Emulirano vrijeme: " + emuliranoVrijeme);
 * </pre>
 * @author Aleksandar
 */
public class EmulatedSystemClock {

    private long startTime;
    private double jitter; //jitter per second,  percentage of deviation per 1 second

    public EmulatedSystemClock() {
        startTime = System.currentTimeMillis();
        Random r = new Random();
        jitter = (r.nextInt(20 )) / 100d; //divide by 10 to get the interval between [0, 20], and then divide by 100 to get percentage
    }

    public long currentTimeMillis() {
        long current = System.currentTimeMillis();
        long diff =current - startTime;
        double coef = diff / 1000;
        return startTime + Math.round(diff * Math.pow((1+jitter), coef));
    }

}