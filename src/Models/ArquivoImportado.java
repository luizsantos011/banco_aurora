package Models;

import Exceptions.LocalizacaoInvalidaException;
import Exceptions.NomeInvalidoException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ArquivoImportado {
    private final String nome;
    private final long tamanho;
    private final LocalDateTime data;
    private final Path localizacao;

    public ArquivoImportado(String nome, long tamanho, Path localizacao) {
        validarNome(nome);
        validarTamanho(tamanho);
        validarLocalizacao(localizacao);
        this.nome = nome;
        this.tamanho = tamanho;
        this.localizacao = localizacao;
        this.data = LocalDateTime.now();
    }

    private void validarNome(String nome) {
        if(nome == null)throw new NullPointerException("Nome do arquivo não pode ser nulo.");
        if(nome.isBlank())throw new IllegalArgumentException("Nome do arquivo não pode ser vazio.");
        if(!nome.toLowerCase().endsWith(".txt") && !nome.toLowerCase().endsWith(".dat")){
            throw new NomeInvalidoException("Nome do arquivo deve terminar com .txt ou .dat");
        }
    }

    private void validarTamanho(long tamanho) {
        if(tamanho <= 0 ){
            throw new IllegalArgumentException("Tamanho do arquivo deve ser maior que zero.");
        }
    }

    private void validarLocalizacao(Path localizacao) {
        if(localizacao == null){
            throw new NullPointerException("Localização do arquivo não pode ser nula.");
        }
        if(!Files.isRegularFile(localizacao)){
            throw new LocalizacaoInvalidaException("Localização do arquivo deve ser um arquivo regular.");
        }
    }

    public String getNome() {
        return nome;
    }

    public long getTamanho() {
        return tamanho;
    }

    public LocalDateTime getData() {
        return data;
    }

    public Path getLocalizacao() {
        return localizacao;
    }
}
