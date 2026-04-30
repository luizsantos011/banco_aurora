package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroAuditoria {
    private final LocalDateTime momento;
    private final String tipo;
    private final String mensagem;

    public RegistroAuditoria(String tipo, String mensagem) {
        validarTipo(tipo);
        validarMensagem(mensagem);
        this.momento = LocalDateTime.now();
        this.tipo = tipo.toUpperCase();
        this.mensagem = mensagem;
    }

    private void validarTipo(String tipo) {
        if (tipo == null) {
            throw new NullPointerException("O tipo não pode ser nulo.");
        }
        if(tipo.isBlank()) {
            throw new IllegalArgumentException("O tipo do log não pode ser vazio.");
        }
    }

    private void validarMensagem(String mensagem) {
        if (mensagem == null ) {
            throw new NullPointerException("A mensagem de auditoria não pode ser nula.");
        }
        if(mensagem.isBlank()) {
            throw new IllegalArgumentException("A mensagem de auditoria não pode ser vazia.");
        }
    }

    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return String.format("[%s] %s: %s", momento.format(formato), tipo, mensagem);
    }

    public LocalDateTime getMomento() { return momento; }
    public String getTipo() { return tipo; }
    public String getMensagem() { return mensagem; }
}