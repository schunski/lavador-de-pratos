package br.edu.utfpr.lavador;

import java.util.concurrent.ThreadLocalRandom;

final class Simulacao {
    private Simulacao() {}

    static long contarAte(int limite) {
        long soma = 0;
        for (int contador = 1; contador <= limite; contador++) {
            soma += contador;
        }
        return soma;
    }

    static int sortearIteracoesEnxugamento() {
        return ThreadLocalRandom.current().nextInt(3, 11) * 10_000_000;
    }
}
