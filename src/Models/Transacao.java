package Models;

import Exceptions.ContaInvalidaException;
import Exceptions.ValorInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transacao {
    private final String origem;
    private final String destino;
    private final BigDecimal valor;
    private final LocalDateTime dataTransacao;

    public Transacao(String origem, String destino,  BigDecimal valor) {
        validarContas(origem, destino);
        validarValor(valor);
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.dataTransacao = LocalDateTime.now();
    }

    private void validarContas(String origem, String destino) {
        if(origem == null || destino == null || origem.isBlank() || destino.isBlank()) {
            throw new ContaInvalidaException("Contas de origem e destino devem ser informadas!");
        }
        if(origem.equals(destino)) {
            throw new ContaInvalidaException("As contas não podem ser iguais!");
        }
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("Valor da transação inválido!");
        }
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
