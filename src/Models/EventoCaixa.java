package Models;

import Exceptions.DataInvalidaException;
import Exceptions.IdInvalidoException;
import Exceptions.OperacaoInvalidaException;

import java.time.LocalDateTime;

public class EventoCaixa {
    private final String idEvento;
    private final LocalDateTime dataProcessamentoCaixa;
    private final int idTerminal;
    private final byte tipoOperacao;

    public EventoCaixa(String idEvento, LocalDateTime dataProcessamentoCaixa, int idTerminal, byte tipoOperacao) {
        validarIdEvento(idEvento);
        validarDataProcessamento(dataProcessamentoCaixa);
        validarIdTerminal(idTerminal);
        validarTipoOperacao(tipoOperacao);
        this.idEvento = idEvento;
        this.dataProcessamentoCaixa = dataProcessamentoCaixa;
        this.idTerminal = idTerminal;
        this.tipoOperacao = tipoOperacao;
    }

    private void validarIdEvento(String idEvento) {
        if(idEvento == null || idEvento.isEmpty()){
            throw new IdInvalidoException("ID nulo ou vazio!");
        }
        if(!idEvento.matches("[A-Z]{4}-\\d{4}-\\d")){
            throw new IdInvalidoException("ID do evento deve seguir o formato 'XXXX-1234-1'!");
        }
    }
    private void validarDataProcessamento(LocalDateTime dataProcessamento) {
        if(dataProcessamento == null || dataProcessamento.isAfter(LocalDateTime.now())){
            throw new DataInvalidaException("Data de processamento inválida!");
        }
    }

    private void validarIdTerminal(int idTerminal) {
        if(idTerminal <= 0 || idTerminal > 16){
            throw new IdInvalidoException("ID do terminal deve ser entre 1 e 16!");
        }
    }
    private void validarTipoOperacao(byte tipoOperacao) {
        if(tipoOperacao < 0 || tipoOperacao > 2){
            throw new OperacaoInvalidaException("Tipo de operação deve ser 0 (entrada), 1 (saída) ou 2 (manutenção)!");
        }
    }

    public String getIdEvento() {
        return idEvento;
    }

    public LocalDateTime getDataProcessamentoCaixa() {
        return dataProcessamentoCaixa;
    }

    public int getIdTerminal() {
        return idTerminal;
    }

    public byte getTipoOperacao() {
        return tipoOperacao;
    }
}
