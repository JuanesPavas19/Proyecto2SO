import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CountDownLatch;

public class Minero extends Robot implements Runnable {
    private static Lock lock = new ReentrantLock();
    private static Lock lock1 = new ReentrantLock();
    private CountDownLatch minerosLatch; // Contador para ejecutar a los siguientes robots en la entrada
    private int identificador;  // Reconocer cual minero es el que está minando y diferenciar su ejecución con el otro
    private static boolean primerMinero = true; // Variable para rastrear el primer minero
    private static int contador = 0; // Cuanto se ha sacado de una sola mena
    private static int menas_acabadas = 0; // Cuantas menas se han dejado ya vacias
    private static int silo = 0; // Para la visualizacion por consola

    public Minero(int Street, int Avenue, Direction direction, int beepers, Color color, CountDownLatch minerosLatch,
            int identificador) {
        super(Street, Avenue, direction, beepers, color);
        this.minerosLatch = minerosLatch;
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
    // izq
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

    // Método de Entrada a la Mina
    public void entrada() {
        lock.lock();
        try {
            recto();
            giroIzquierda();
            recto(1);
        } finally {
            lock.unlock();
        }
        giroDerecha();
        recto();
        minerosLatch.countDown(); // Disminuir el contador de minerosLatch una vez que el minero comienza a moverse
        giroDerecha();
        recto();
        giroIzquierda();
        recto();
        giroIzquierda();
        recto();
        giroIzquierda();
        recto();
        giroDerecha();

        lock.lock();
        try {
            if (primerMinero) {
                recto(6); // Primer minero va hasta la primera mena
            } else {
                recto(5); // Segundo minero va una posición menos
                giroDerecha();
                recto(1);
                giroIzquierda();
                recto(1);
                giroIzquierda();    // Se acomoda en el cajón de espera
            }
        } finally {
            primerMinero = false; // Marcamos que el primer minero ya ha pasado
            lock.unlock();
        }
    }

    // Método para extraer los Beepers de la Mina
    public void minar() { // Perdón por este código, pero es lo que nos funcionó :(
        while (contador <= 2000) { // Se pone la cantidad de menas que hay en cada silo (2000 en el ejercicio)
            silo = menas_acabadas + 1;      // Simplemente para leer por consola en que silo está actualmente minando
            if (contador < 2000) {
                if (identificador == 1) { // Para el minero 1
                    lock1.lock();
                    try {
                        if (primerMinero == false) { // Cuando llega, se ubica en la mena, entonces no tiene que hacer el recorrido de acomodación
                            for (int i = 0; i < 50; i++) { // Recoge su maxima capacidad (50 en el ejercicio)
                                pickBeeper();
                                contador++;
                                System.out.println("Beepers Extraidos: " + contador + " - Silo: " + silo);
                            }
                            cambioSentido();
                            System.out.println("Menas Acabadas: " + menas_acabadas);
                            recto(menas_acabadas); // Se ubica en el punto de semáforo con trenes, antes del punto de entrega para dejar los beepers
                            primerMinero = true;    // Para que nunca se repita este caso, ya que no debe acomodarse desde la entrada
                        } else { // Cuando ya hizo la primera iteracion, se comporta igual que el otro minero
                            recto(1);
                            giroDerecha();
                            recto(menas_acabadas); // Se posiciona en la mena actual a la que debe ir
                            for (int i = 0; i < 50; i++) { // Recoge su maxima capacidad (50 en el ejercicio)
                                pickBeeper();
                                contador++; // La cantidad de beepers que han sido extraidos aumenta (para saber cuando debe cambiar de silo)
                                System.out.println("Beepers Extraidos: " + contador + " - Silo: " + silo);
                            }
                            cambioSentido();
                            System.out.println("Menas Acabadas: " + menas_acabadas);
                            recto(menas_acabadas); // Se ubica en el punto de semáforo con trenes, antes del punto de entrega para dejar los beepers
                        }
                        try {
                            Controlador_Semaforos.semaforo_mineros_2.acquire(); // Solicita el semáforo que tiene con los trenes
                            recto(1);   // Se mueve al punto de recolección
                            for (int i = 0; i < 50; i++) { // Coloca los beepers en el punto de recolección
                                putBeeper();
                            }
                            giroIzquierda();
                            recto(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Controlador_Semaforos.semaforo_trenes_2.release(); // Libera el semáforo para que el tren pueda actuar
                        }
                    } finally {
                        lock1.unlock();
                    }
                    giroIzquierda();
                    recto(1);
                    giroIzquierda();

                } else if (identificador == 2) { // Para el minero 2
                    lock1.lock();
                    try {
                        recto(1);
                        giroDerecha();
                        recto(menas_acabadas); // Se posiciona en la mena actual a la que deben ir
                        for (int i = 0; i < 50; i++) { // Recoge su maxima capacidad (50 en el ejercicio)
                            pickBeeper();
                            contador++; // La cantidad de beepers que han sido extraidos aumenta (para saber cuando debe
                                        // cambiar de silo)
                            System.out.println("Beepers Extraidos: " + contador + " - Silo: " + silo);
                        }
                        cambioSentido();
                        System.out.println("Menas Acabadas: " + menas_acabadas);
                        recto(menas_acabadas); // Se ubica en el punto de entrega para dejar los beepers
                        try {
                            Controlador_Semaforos.semaforo_mineros_2.acquire(); // Solicita el semaforo que tiene con los trenes
                            recto(1);   // Se mueve al punto de recolección
                            for (int i = 0; i < 50; i++) { // Coloca los beepers en el punto de recolección
                                putBeeper();
                            }
                            giroIzquierda();
                            recto(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            Controlador_Semaforos.semaforo_trenes_2.release(); // Libera el semáforo para que el tren pueda actuar
                        }
                    } finally {
                        lock1.unlock();
                    }
                    giroIzquierda();
                    recto(1);
                    giroIzquierda();
                }
            } else {
                menas_acabadas++; // Si el silo actual ya se vació, pasa al siguiente
                contador = 0; // Empieza de 0 para el nuevo silo
                if (menas_acabadas > 5) { // En caso de que los silos queden vacios, los robots se apagan (por ahora)
                    turnOff(); // Tiene un bug extraño que literal al apagarse uno, el otro no puede terminar su ejecución
                } // pero no acaba aqui el robot entonces por ahora no importa
            }
        }
    }

    public void race() {
        // Las acciones del minero
        entrada();
        minar();
    }

    @Override
    public void run() {
        race();
    }
}