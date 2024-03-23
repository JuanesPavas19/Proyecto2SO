import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CountDownLatch;

public class Tren extends Robot implements Runnable {
    private static Lock lock = new ReentrantLock();
    private CountDownLatch trenesLatch;     // Contador para ejecutar a los siguientes robots en la entrada
    private static boolean primerTren = true;   // Variable para rastrear el primer Tren
    private static boolean segundoTren = true;  // Variable para rastrear el segundo Tren
    private boolean salida = true;  // Variable para marcar cuando romper el ciclo

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

    public void setSalida(boolean valor) { // cambiar valor de salida
        this.salida = valor;
    }

    // Método de Entrada a la Mina
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
    }

    // Método del Ciclo de ejecución de los Trenes
    public void ciclo() {
        while (salida) { // cuando su variable pasa a false desde el controladorTrenes se marca la salidade cada tren del ciclo
            recto();
            giroIzquierda();
            recto(4); // Semaforo 1 vertical
            try {   // Semáforo de la intersección vertical de la mina
                // if (Controlador_Semaforos.permitirPasarVerticalSector1()) {
                // Controlador_Semaforos.semaforo_trenes_vertical_1.acquire();
                // recto(2); // Avanzar
                // }
                Controlador_Semaforos.semaforo_trenes_vertical_1.acquire();
                recto(2); // Avanzar
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_trenes_horizontal_3.release();
            }
            recto();
            giroDerecha();
            recto(4); // Semaforo 2
            try {       // Semáforo para recoger beepers extraídos por Mineros
                Controlador_Semaforos.semaforo_trenes_2.acquire(); // Conseguir la luz verde del semaforo
                recto(1); // punto de recoleccion
                for (int i = 0; i < 50; i++) { // Cuantos beepers recoge, se hace asi por la eficiencia for > while
                    pickBeeper();
                }
                giroDerecha();
                recto(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_mineros_2.release(); // Dar la luz verde a los mineros
            }
            recto();
            giroDerecha();
            recto(4); // Semaforo 3
            try {   // Semáforo de la intersección horizontal de la mina
                // if (Controlador_Semaforos.permitirPasarHorizontalSector3()) {
                // Controlador_Semaforos.semaforo_trenes_horizontal_3.acquire();
                // recto(2); // AvanzarF
                // }
                Controlador_Semaforos.semaforo_trenes_horizontal_3.acquire();
                recto(2); // AvanzarF
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_trenes_vertical_1.release();
            }
            recto();
            giroIzquierda();
            recto(4); // Semaforo 4
            try {       // Semáforo para entregar beepers y que los Extractores se los lleven
                Controlador_Semaforos.semaforo_trenes_4.acquire();
                recto();
                giroDerecha();
                recto(1);
                while (anyBeepersInBeeperBag()) { // Entregar todos los beepers que carga en el punto de recoleccion
                    putBeeper();
                }
                cambioSentido();
                recto(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Controlador_Semaforos.semaforo_extractores_4.release(); // Dar la luz verde a los extractores
            }
        }
    }

    // Método para sacarlos de la Mina (tiene errores dado a los bloqueos impuestos por los semáforos)
    public void salida() {
        // turnOff(); // solo matarlos para pruebas
        recto(1);
        giroDerecha();
        recto();
        giroDerecha();
        recto();
        giroIzquierda();
        recto(5);
    }

    public void race() {
        // Las acciones del tren
        entrada();
        ciclo();
        salida();
    }

    @Override
    public void run() {
        race();
    }
}
