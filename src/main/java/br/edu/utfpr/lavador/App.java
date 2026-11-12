package br.edu.utfpr.lavador;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final long DURACAO_MILIS = 60_000;

    private final Escorredor escorredor;
    private final Lavador[] lavadores;
    private final Enxugador[] enxugadores;
    private final Thread[] threadsLavadores;
    private final Thread[] threadsEnxugadores;
    private boolean iniciada;
    private boolean encerrada;

    public App(int quantidadeLavadores, int quantidadeEnxugadores, int capacidade) {
        if (quantidadeLavadores <= 0 || quantidadeEnxugadores <= 0) {
            throw new IllegalArgumentException("E necessario pelo menos um lavador e um enxugador.");
        }
        escorredor = new Escorredor(capacidade);
        lavadores = new Lavador[quantidadeLavadores];
        enxugadores = new Enxugador[quantidadeEnxugadores];
        threadsLavadores = new Thread[quantidadeLavadores];
        threadsEnxugadores = new Thread[quantidadeEnxugadores];
        for (int i = 0; i < lavadores.length; i++) {
            lavadores[i] = new Lavador(escorredor);
            threadsLavadores[i] = new Thread(lavadores[i], "Lavador-" + (i + 1));
        }
        for (int i = 0; i < enxugadores.length; i++) {
            enxugadores[i] = new Enxugador(escorredor);
            threadsEnxugadores[i] = new Thread(enxugadores[i], "Enxugador-" + (i + 1));
        }
    }

    public synchronized void iniciar() {
        if (iniciada || encerrada) {
            throw new IllegalStateException("A aplicacao so pode ser iniciada uma vez.");
        }
        iniciada = true;
        for (Thread thread : threadsEnxugadores) {
            thread.start();
        }
        for (Thread thread : threadsLavadores) {
            thread.start();
        }
        LOGGER.info("Trabalhos iniciados com " + lavadores.length + " lavador(es) e "
                + enxugadores.length + " enxugador(es).");
    }

    public synchronized void parar() {
        if (encerrada) {
            return;
        }
        encerrada = true;
        for (Lavador lavador : lavadores) {
            lavador.parar();
        }
        interromper(threadsLavadores);
        boolean interrompido = aguardarTermino(threadsLavadores);

        for (Enxugador enxugador : enxugadores) {
            enxugador.parar();
        }
        interromper(threadsEnxugadores);
        if (aguardarTermino(threadsEnxugadores)) {
            interrompido = true;
        }

        LOGGER.info("Trabalhos encerrados. Lavados: " + escorredor.getTotalColocados()
                + "; enxugados: " + getTotalEnxugados()
                + "; restantes no escorredor: " + escorredor.getQuantidade() + ".");
        if (interrompido) {
            Thread.currentThread().interrupt();
        }
    }

    public long getTotalLavados() {
        return escorredor.getTotalColocados();
    }

    public long getTotalEnxugados() {
        long total = 0;
        for (Enxugador enxugador : enxugadores) {
            total += enxugador.getTotalEnxugados();
        }
        return total;
    }

    public int getPratosRestantes() {
        return escorredor.getQuantidade();
    }

    private static void interromper(Thread[] threads) {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    private static boolean aguardarTermino(Thread[] threads) {
        boolean interrompido = false;
        for (Thread thread : threads) {
            while (thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    interrompido = true;
                }
            }
        }
        return interrompido;
    }

    public static void main(String[] args) {
        Logger raiz = Logger.getLogger("");
        for (Handler handler : raiz.getHandlers()) {
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord registro) {
                    return registro.getLevel().getName() + ": "
                            + formatMessage(registro) + System.lineSeparator();
                }
            });
        }
        if (args.length > 4) {
            LOGGER.severe("Uso: App [lavadores] [enxugadores] [capacidade] [INFO|FINE]");
            return;
        }
        App app;
        try {
            int lavadores = 2;
            int enxugadores = 2;
            int capacidade = 10;
            Level nivel = Level.INFO;

            if (args.length > 0) {
                lavadores = Integer.parseInt(args[0]);
            }
            if (args.length > 1) {
                enxugadores = Integer.parseInt(args[1]);
            }
            if (args.length > 2) {
                capacidade = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                nivel = Level.parse(args[3]);
            }
            raiz.setLevel(nivel);
            for (Handler handler : raiz.getHandlers()) {
                handler.setLevel(nivel);
            }
            app = new App(lavadores, enxugadores, capacidade);
        } catch (IllegalArgumentException e) {
            LOGGER.severe("Configuracao invalida: " + e.getMessage());
            return;
        }

        app.iniciar();
        try {
            Thread.sleep(DURACAO_MILIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            app.parar();
        }
    }
}
