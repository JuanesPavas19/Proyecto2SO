import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CountDownLatch;

public class Tren extends Robot implements Runnable {
    private static Lock lock = new ReentrantLock();
    private CountDownLatch trenesLatch;
    private static boolean primerTren = true; // Variable para rastrear el primer Tren
    private static boolean segundoTren = true;

    public Tren(int Street, int Avenue, Direction direction, int beepers, Color color, CountDownLatch trenesLatch) {
        super(Street, Avenue, direction, beepers, color);
        this.trenesLatch = trenesLatch;
        World.setupThread(this);
    }

    // El robot solo puede girar a la izquierda
    public void giro(int num_reps) {
        for (int i = 0; i < num_reps; i++) {
            turnLeft();
        }
    }

    // Método para girar a la derecha (equivalente a girar tres veces a la
    // izquierda)
    public void giroDerecha() {
        giro(3);
    }

    public void giroIzquierda() {
        giro(1);
    }

    // Método para girar en sentido contrario (equivalente a girar dos veces a la
    public void cambioSentido() {
        giro(2);
    }

    // metodos recto sobrecarga, uno infinito y otro especifico
    public void recto() {
        while (frontIsClear()) {
            move();
        }
    }

    public void recto(int num_reps) {
        for (int i = 0; i < num_reps; i++) {
            move();
        }
    }

    public void entrada() {
        lock.lock();
        try {
            recto();
        } finally {
            lock.unlock();
        }
        trenesLatch.countDown(); // Disminuir el contador de trenesLatch una vez que el tren comience a moverse
        giroDerecha();
        recto();
        giroIzquierda();
        recto();
        giroIzquierda();
        recto(2);
        // lock.lock();
        // try {
        // if (primerTren) {
        // recto(); // Primer minero va al fondo
        // // lock.unlock();
        // } else if (segundoTren) {
        // recto(5); // Segundo minero va una posición menos
        // // lock.unlock();
        // } else {
        // recto(3);
        // }
        // } finally {
        // primerTren = false; // Marcamos que el primer minero ya ha pasado
        // segundoTren = false;
        // lock.unlock();
        // }
    }

    public void ciclo() {
        while (true) {
            recto();
            giroIzquierda();
            recto();    // Semaforo 1
            giroDerecha();
            recto(4); // Semaforo 2
            try {
                Controlador_Semaforos.semaforo_trenes_2.acquire();    // Conseguir la luz verde del semaforo
                recto(1);   // punto de recoleccion
                for (int i = 0; i < 20; i++){
                    pickBeeper();
                }
                giroDerecha();
                recto(2);
            } catch (InterruptedException e){
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_mineros_2.release();       // Dar la luz verde a los mineros
            }
            recto();
            giroDerecha();
            recto();    // Semaforo 3
            giroIzquierda();
            recto(4); // Semaforo 4
            try {
                Controlador_Semaforos.semaforo_trenes_4.acquire();
                recto();
                giroDerecha(); 
                recto(1);
                while(anyBeepersInBeeperBag()){
                    putBeeper();
                }
                cambioSentido();
                recto(1);
            } catch (InterruptedException e){
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_extractores_4.release();       // Dar la luz verde a los mineros
            }
        }
    }

    public void race() {
        // Las acciones del tren
        entrada();
        ciclo();
    }

    @Override
    public void run() {
        race();
    }
}
