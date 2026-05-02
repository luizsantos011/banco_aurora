package Models;

import Exceptions.IdInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Lote {
    private final String id;
    private final Estado estado;
    private final LocalDateTime dataCriacao;
    private BigDecimal valorTotal;
    private List<Transacao> transacoes;

    public enum Estado { SE, BA, AL, PE }

    public Lote(Estado estado, int sequencial, LocalDateTime dataCriacao) {
        this.id = String.format("%s-%04d", sequencial);
        this.estado = estado;
        this.dataCriacao = dataCriacao;
        this.valorTotal = BigDecimal.ZERO;
        this.transacoes = new ArrayList<>();
    }

    private void validarSequencial(int sequencial){
        if(sequencial < 1 || sequencial > 9999)throw new IdInvalidoException("Sequencial deve estar entre 1 e 9999");
    }

    public void adicionarTransacao(Transacao t) {
        if (t == null) throw new NullPointerException("Transação nula");
        this.transacoes.add(t);
        this.valorTotal = this.valorTotal.add(t.getValor());
    }

    public String getId() { return id; }
    public Estado getEstado() { return estado; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public List<Transacao> getTransacoes() { return transacoes; }
}