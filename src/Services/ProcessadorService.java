package Services;

import Contracts.*;
import Models.*;
import Repository.ArquivoRepository;
import Exceptions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorService implements IProcessadorService {
    private final ILeitor leitorAgencia;
    private final ILeitor leitorCaixa;
    private final ILote iLote;
    private final ILogger logger;
    private final IArquivoRepository repository;
    private final RelatorioFinal relatorio;
    private final List<Transacao> todasAsTransacoes;

    public ProcessadorService(ILeitor leitorAgencia, ILeitor leitorCaixa, ILote iLote, ILogger logger,
                              IArquivoRepository repository, RelatorioFinal relatorio) {
        this.leitorAgencia = leitorAgencia;
        this.leitorCaixa = leitorCaixa;
        this.iLote = iLote;
        this.logger = logger;
        this.repository = repository;
        this.relatorio = relatorio;
        this.todasAsTransacoes = new ArrayList<>();
    }

    public void processarArquivo(Path caminho) {
        try{
            ILeitor leitor = selecionarLeitor(caminho);
            Transacao t = leitor.lerArquivo(caminho);

        }catch(Exception e){
            System.out.println("Deu ruim ai.");
        }
    }
    private ILeitor selecionarLeitor(Path caminho) {
        String nomeArquivo = caminho.getFileName().toString().toLowerCase();
        if(nomeArquivo.endsWith(".txt")){
            return leitorAgencia;
        }else if(nomeArquivo.endsWith(".bin")){
            return leitorCaixa;
        }else {
            repository.tratarFalhaCritica(caminho);
            throw new FormatoArquivoInvalidoException("Formato de arquivo não suportado: " + nomeArquivo);
        }
    };
}

