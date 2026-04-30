package Models;

import Exceptions.DataInvalidaException;
import Exceptions.IdInvalidoException;
import Exceptions.ValorInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Lote {
    private final String idLote;
    private final LocalDateTime dataProcessamento;
    private final BigDecimal valorTotal;
    private List<Transacao> transacoes;

    public Lote(String idLote, LocalDateTime dataProcessamento, BigDecimal valorTotal) {
        validarIdLote(idLote);
        validarDataProcessamento(dataProcessamento);
        validarValorTotal(valorTotal);
        this.idLote = idLote;
        this.dataProcessamento = dataProcessamento;
        this.valorTotal = valorTotal;
        this.transacoes = new ArrayList<>();
    }
    private void validarIdLote(String idLote) {
        if(idLote == null || idLote.isEmpty()){
            throw new IdInvalidoException("ID do lote não pode ser nulo ou vazio!");
        }
        if(!idLote.matches("[A-Z]{2}-\\d{4}")){
            throw new IdInvalidoException("ID do lote deve seguir o formato 'XX-1234'!");
        }
    }
    private void validarDataProcessamento(LocalDateTime dataProcessamento) {
        if(dataProcessamento == null || dataProcessamento.isAfter(LocalDateTime.now())){
            throw new DataInvalidaException("Data de processamento inválida!");
        }
    }
    private void validarValorTotal(BigDecimal valorTotal) {
        if(valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0){
            throw new ValorInvalidoException("Valor total do lote não pode ser negativo ou nulo!");
        }
    }
    public boolean validarIntegridade(){
        BigDecimal soma = transacoes.stream()
                .map(v -> v.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(valorTotal) == 0;
    }
    public void  adicionarTransacao(Transacao transacao) {
        if(transacoes == null){
            throw new NullPointerException("Lista de transações não pode ser nula!");
        }
        transacoes.add(transacao);
    }

    public String getIdLote() {
        return idLote;
    }
    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    public List<Transacao> getTransacoes() {
        return transacoes;
    }
}
