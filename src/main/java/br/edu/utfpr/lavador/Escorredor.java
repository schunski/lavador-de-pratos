package br.edu.utfpr.lavador;

import java.util.logging.Logger;

public final class Escorredor {
    private static final Logger LOGGER = Logger.getLogger(Escorredor.class.getName());

    private final Prato[] pratos;
    private int inicio;
    private int fim;
    private int quantidade;
    private int aguardando;
    private long totalColocados;
    private long totalRetirados;

    public Escorredor(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser positiva.");
        }
        pratos = new Prato[capacidade];
        registrarEstado();
    }

    public synchronized void colocar(Prato prato) throws InterruptedException {
        if (prato == null) {
            throw new NullPointerException("O prato nao pode ser nulo.");
        }
        while (estaCheio()) {
            aguardar();
        }
        pratos[fim] = prato;
        fim = (fim + 1) % pratos.length;
        quantidade++;
        totalColocados++;
        LOGGER.fine("Colocado: " + prato + "; quantidade: " + quantidade);
        sinalizarLimite();
    }

    public synchronized Prato retirar() throws InterruptedException {
        while (estaVazio()) {
            aguardar();
        }
        Prato prato = pratos[inicio];
        pratos[inicio] = null;
        inicio = (inicio + 1) % pratos.length;
        quantidade--;
        totalRetirados++;
        LOGGER.fine("Retirado: " + prato + "; quantidade: " + quantidade);
        sinalizarLimite();
        return prato;
    }

    public synchronized boolean estaCheio() {
        return quantidade == pratos.length;
    }

    public synchronized boolean estaVazio() {
        return quantidade == 0;
    }

    public synchronized int getQuantidade() {
        return quantidade;
    }

    public synchronized long getTotalColocados() {
        return totalColocados;
    }

    public synchronized long getTotalRetirados() {
        return totalRetirados;
    }

    synchronized void aguardar() throws InterruptedException {
        aguardando++;
        try {
            LOGGER.fine(Thread.currentThread().getName() + " aguardando; quantidade: " + quantidade);
            wait();
        } finally {
            aguardando--;
        }
    }

    private void sinalizarLimite() {
        if (estaVazio() || estaCheio()) {
            registrarEstado();
            for (int i = 0; i < aguardando; i++) {
                notify();
            }
        }
    }

    private void registrarEstado() {
        String estado = "CHEIO";
        if (quantidade == 0) {
            estado = "VAZIO";
        }
        LOGGER.info("Escorredor " + estado
                + ": " + quantidade + "/" + pratos.length + " pratos.");
    }
}
