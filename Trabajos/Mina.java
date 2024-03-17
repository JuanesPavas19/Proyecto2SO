import kareltherobot.*;
import java.awt.Color;

public class Mina implements Directions {
    public static void main(String[] args) {
        World.readWorld("Mina.kwld");
        World.setVisible(true);

        int numMineros = 0;
        Color negro = new Color(0, 0, 0);

        int numTrenes = 0;
        Color azul = new Color(0, 0, 255);

        int numExtractores = 0;
        Color rojo = new Color(255, 0, 0);

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m")) {
                numMineros = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-t")) {
                numTrenes = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-e")) {
                numExtractores = Integer.parseInt(args[++i]);
            }
        }

        if (numMineros < 2)
            numMineros = 2;
        if (numTrenes < 2)
            numTrenes = 2;
        if (numExtractores < 2)
            numExtractores = 2;

        Thread mineros[] = new Thread[numMineros];
        Thread trenes[] = new Thread[numTrenes];
        Thread extractores[] = new Thread[numExtractores];

        // Crea los objetos especificados
        for (int i = 0; i < numMineros; i++) {
            Minero minero = new Minero(12+i, 1, South, 0, negro);
            Thread mineroThread = new Thread(minero);
            mineros[i] = mineroThread;
            System.out.println("Se creó un objeto Minero");
        }

        for (int i = 0; i < numTrenes; i++) {
            Tren tren = new Tren(12+i, 2, South, 0, azul);
            Thread trenThread = new Thread(tren);
            trenes[i] = trenThread;
            System.out.println("Se creó un objeto Tren");
        }

        for (int i = 0; i < numExtractores; i++) {
            Extractor extractor = new Extractor(12+i, 3, South, 0, rojo);
            Thread extractorThread = new Thread(extractor);
            extractores[i] = extractorThread;
            System.out.println("Se creó un objeto Extractor");
        }

        for (Thread hilo:mineros){
            hilo.start();
        }

        for (Thread hilo:extractores){
            hilo.start();
        }
    }
}