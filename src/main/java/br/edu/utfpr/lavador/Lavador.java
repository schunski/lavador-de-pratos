package br.edu.utfpr.lavador;

import java.util.logging.Logger;

public final class Lavador implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Lavador.class.getName());

    private final Escorredor escorredor;
    private volatile boolean done;

    public Lavador(Escorredor escorredor) {
        if (escorredor == null) {
            throw new NullPointerException("O escorredor nao pode ser nulo.");
        }
        this.escorredor = escorredor;
    }

    public void parar() {
        done = true;
    }

    @Override
    public void run() {
        LOGGER.fine(Thread.currentThread().getName() + " iniciou.");
        try {
            while (!done) {
                synchronized (escorredor) {
                    while (escorredor.estaCheio() && !done) {
                        escorredor.aguardar();
                    }
                    if (done) {
                        break;
                    }
                }

                Prato prato = new Prato();
                LOGGER.fine(Thread.currentThread().getName() + " lavando " + prato);
                long soma = Simulacao.contarAte(prato.getNivelSujeira().getIteracoesLavagem());
                LOGGER.fine(Thread.currentThread().getName() + " terminou de lavar " + prato
                        + "; soma da contagem: " + soma);
                colocarPratoLavado(prato);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            LOGGER.fine(Thread.currentThread().getName() + " encerrou.");
        }
    }

    private void colocarPratoLavado(Prato prato) {
        boolean interrompido = false;
        try {
            while (true) {
                try {
                    escorredor.colocar(prato);
                    return;
                } catch (InterruptedException e) {
                    interrompido = true;
                }
            }
        } finally {
            if (interrompido) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
