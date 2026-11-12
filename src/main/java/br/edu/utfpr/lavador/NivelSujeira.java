package br.edu.utfpr.lavador;

public enum NivelSujeira {
    BAIXO(30_000_000),
    MEDIO(50_000_000),
    ENGORDURADO(100_000_000);

    private final int iteracoesLavagem;

    NivelSujeira(int iteracoesLavagem) {
        this.iteracoesLavagem = iteracoesLavagem;
    }

    public int getIteracoesLavagem() {
        return iteracoesLavagem;
    }
}
