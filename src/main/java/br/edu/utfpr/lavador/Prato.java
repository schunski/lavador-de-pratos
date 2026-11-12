package br.edu.utfpr.lavador;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public final class Prato {
    private static final AtomicLong PROXIMO_NUMERO = new AtomicLong();

    private final long numeroSerie;
    private final NivelSujeira nivelSujeira;

    public Prato() {
        numeroSerie = PROXIMO_NUMERO.incrementAndGet();
        int sorteio = ThreadLocalRandom.current().nextInt(100);
        if (sorteio < 30) {
            nivelSujeira = NivelSujeira.BAIXO;
        } else if (sorteio < 90) {
            nivelSujeira = NivelSujeira.MEDIO;
        } else {
            nivelSujeira = NivelSujeira.ENGORDURADO;
        }
    }

    public long getNumeroSerie() {
        return numeroSerie;
    }

    public NivelSujeira getNivelSujeira() {
        return nivelSujeira;
    }

    @Override
    public String toString() {
        return "Prato #" + numeroSerie + " (" + nivelSujeira + ")";
    }
}
