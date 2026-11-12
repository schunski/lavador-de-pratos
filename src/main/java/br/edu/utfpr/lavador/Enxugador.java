package br.edu.utfpr.lavador;

import java.util.logging.Logger;

public final class Enxugador implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Enxugador.class.getName());

    private final Escorredor escorredor;
    private volatile boolean done;
    private long totalEnxugados;

    public Enxugador(Escorredor escorredor) {
        if (escorredor == null) {
            throw new NullPointerException("O escorredor nao pode ser nulo.");
        }
        this.escorredor = escorredor;
    }

    public void parar() {
        done = true;
    }

    public long getTotalEnxugados() {
        return totalEnxugados;
    }

    @Override
    public void run() {
        LOGGER.fine(Thread.currentThread().getName() + " iniciou.");
        try {
            while (true) {
                Prato prato;
                synchronized (escorredor) {
                    while (escorredor.estaVazio() && !done) {
                        try {
                            escorredor.aguardar();
                        } catch (InterruptedException e) {
                            if (!done) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }
                    if (escorredor.estaVazio() && done) {
                        break;
                    }
                    Thread.interrupted();
                    prato = escorredor.retirar();
                }
                int iteracoes = Simulacao.sortearIteracoesEnxugamento();
                LOGGER.fine(Thread.currentThread().getName()
                        + " enxugando " + prato + "; iteracoes: " + iteracoes);
                long soma = Simulacao.contarAte(iteracoes);
                totalEnxugados++;
                LOGGER.fine(Thread.currentThread().getName() + " terminou de enxugar " + prato
                        + "; soma da contagem: " + soma);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.fine(Thread.currentThread().getName() + " encerrou.");
        }
    }
}
