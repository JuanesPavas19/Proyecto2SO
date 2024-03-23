import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;

public class Extractor extends Robot implements Runnable {
    private static Lock lock = new ReentrantLock();
    private static Lock lock1 = new ReentrantLock();
    private static Semaphore semaforo1 = new Semaphore(1);
    private static Semaphore semaforo2 = new Semaphore(0);
    private static Semaphore semaforo3 = new Semaphore(1);
    private static Semaphore semaforo4 = new Semaphore(0);
    private CountDownLatch extractoresLatch;
    private int identificador;
    private static boolean primerExtractor = true; // Variable para rastrear el primer Extractor
    private boolean primerExtractorCompleto = false;
    private static int contador = 0; // Cuantos beepers se han dejado en el punto de extraccion
    private static int columnas = 5; // Columna en donde almacena
    private static int beepers1 = 0; // Cuantos beepers ha recogido el numero 1 (para detener la extraccion)
    private static int beepers2 = 0; // Cuantos beepers ha recogido el numero 2 (para detener la extraccion)

    public Extractor(int Street, int Avenue, Direction direction, int beepers, Color color,
            CountDownLatch extractoresLatch, int identificador) {
        super(Street, Avenue, direction, beepers, color);
        this.extractoresLatch = extractoresLatch;
        this.identificador = identificador;
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
        lock1.lock();
        try {
            // Cambiate esto
            giroDerecha();
            recto(1);
            giroIzquierda();
            recto();
        } finally {
            lock1.unlock();
        }
        giroDerecha();
        recto(1);
        extractoresLatch.countDown();
        giroIzquierda();
        lock.lock();
        try {
            if (primerExtractor) {
                recto(); // Primer extractor va al fondo
                giroIzquierda();
            } // El segundo se queda quieto en la posicion indicada previa al lock
        } finally {
            primerExtractor = false; // Marcamos que el primer extractor ya ha pasado
            lock.unlock();
        }
    }

    public void extraccion_mina() { // Habrá manera de optimizar la extracción?
        while (true) {
            if (identificador == 1) { // Primer Extractor (Dentro de la mina)
                if (beepers1 == 12000) { // Cuando haya sacado los 12000 (condicion de parada)
                    break;
                }
                try {
                    Controlador_Semaforos.semaforo_extractores_4.acquire();
                    recto(1);
                    cambioSentido();
                    for (int i = 0; i < 50; i++) { // Capacidad de 50 beepers de los extractores
                        pickBeeper();
                    }
                    recto();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Controlador_Semaforos.semaforo_trenes_4.release();
                }
                giroDerecha();
                recto(4);
                try {
                    semaforo1.acquire();
                    recto(1);
                    while (anyBeepersInBeeperBag()) { // Entrega al segundo punto de extracción
                        putBeeper();
                        beepers1++;
                    }
                    cambioSentido(); // Empieza el retorno a la posición inicial
                    recto(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforo2.release();
                }
                recto();
                giroIzquierda();

            } else if (identificador == 2) { // Segundo Extractor (Para afuera de la mina)
                if (beepers2 == 12000) { // Cuando haya sacado los 12000 (condicion de parada)
                    break;
                }
                try {
                    semaforo2.acquire();
                    recto(1);
                    for (int i = 0; i < 50; i++) { // Recoleccion en el segundo punto de extracción
                        pickBeeper(); // Capacidad de 50 beepers de los extractores
                    }
                    cambioSentido();
                    recto();
                    if (contador >= 3000) { // Si un almacen se llena
                        columnas--;
                        contador = 0;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaforo1.release();
                }
                giroDerecha();
                recto();
                giroIzquierda();
                recto(1);
                giroDerecha();
                recto(columnas);
                giroDerecha();
                recto();
                cambioSentido();
                lock1.lock();
                try {
                    while (anyBeepersInBeeperBag()) { // Entrega en el silo vacío del almacén
                        putBeeper();
                        contador++;
                        beepers2++;
                        System.out.println("Contador = " + contador);
                    }
                    recto(1);
                    giroIzquierda();
                    recto(columnas);
                    giroIzquierda();
                    recto();
                    giroDerecha();
                    recto();
                    giroIzquierda();
                } finally {
                    lock1.unlock();
                }
            }
        }
    }

    public void salida() { // Metodo para sacar a los robots
        if (identificador == 2) { // Debe salir de primero el ultimo
            try {
                semaforo3.acquire();
                giroIzquierda();
                recto();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaforo4.release();
            }
            giroIzquierda();
            recto(1);
            giroIzquierda();
            recto();
            giroDerecha();
            recto(6);
            turnOff();
        } else if (identificador == 1) {
            try {
                semaforo4.acquire();
                giroIzquierda();
                recto();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaforo3.release();
            }
            giroDerecha();
            recto();
            giroIzquierda();
            recto(1);
            giroIzquierda();
            recto();
            giroDerecha();
            recto(5);
            turnOff();
        }
    }

    public void race() {
        // Las acciones del extractor
        entrada();
        extraccion_mina();
        salida();
    }

    @Override
    public void run() {
        race();
    }
}
