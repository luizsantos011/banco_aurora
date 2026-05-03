package Services;

import Contracts.*;
import Models.*;
import Exceptions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProcessadorService implements IProcessadorService {
    private final ILeitor leitorAgencia;
    private final ILeitor leitorCaixa;
    private final ILoteService iLoteService;
    private final ILogger logger;
    private final IArquivoRepository repository;
    private final RelatorioFinal relatorio;

    public ProcessadorService(ILeitor leitorAgencia, ILeitor leitorCaixa, ILoteService iLoteService, ILogger logger,
                              IArquivoRepository repository, RelatorioFinal relatorio) {
        this.leitorAgencia = leitorAgencia;
        this.leitorCaixa = leitorCaixa;
        this.iLoteService = iLoteService;
        this.logger = logger;
        this.repository = repository;
        this.relatorio = relatorio;
    }

    @Override
    public void processarArquivo(Path caminho) {
        ArquivoImportado arquivoImportado = null;
        try{
            ILeitor leitor = selecionarLeitor(caminho);
            Transacao t = leitor.lerArquivo(caminho);
            arquivoImportado = repository.preparar(caminho);
            iLoteService.adicionarTransacao(t);
            relatorio.registrarSucesso(t.getValor());
            relatorio.incrementarArquivos();
            logger.registrarSucesso("Arquivo processado com sucesso: " + caminho.getFileName());
        }catch(IOException e){
            relatorio.registrarFalha();
            logger.registrarErro("Erro ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
        }catch(FormatoArquivoInvalidoException | ValorInvalidoException | OperacaoInvalidaException e){
            relatorio.registrarFalha();
            logger.registrarErro("Erro ao processar arquivo: " + caminho.getFileName());
        }catch (RuntimeException e){
            relatorio.registrarFalha();
            logger.registrarErro("Erro inesperado ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
        }catch (Exception e) {
            relatorio.registrarFalha();
            logger.registrarErro("Erro inesperado ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
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

