package Services;

import Contracts.*;
import Models.*;
import Exceptions.*;
import java.io.IOException;
import java.nio.file.Path;

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
        try {
            logger.registrarSucesso("Iniciando processamento do arquivo: " + caminho.getFileName());

            ILeitor leitor = selecionarLeitor(caminho);
            Transacao t = leitor.lerArquivo(caminho);

            arquivoImportado = repository.preparar(caminho);

            iLoteService.adicionarTransacao(t);
            relatorio.registrarSucesso(t.getValor());
            relatorio.incrementarArquivos();

            repository.finalizarComSucesso(arquivoImportado);
            logger.registrarSucesso("Arquivo processado e finalizado com sucesso: " + caminho.getFileName());

        } catch (IOException e) {
            relatorio.registrarFalha();
            logger.registrarErro("Erro de E/S ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
            repository.tratarFalha(caminho);
        } catch (FormatoArquivoInvalidoException | ValorInvalidoException | OperacaoInvalidaException e) {
            relatorio.registrarFalha();
            logger.registrarErro("Erro de validação de negócio no arquivo: " + caminho.getFileName() + " - " + e.getMessage());
            repository.tratarFalha(caminho);
        } catch (RuntimeException e) {
            relatorio.registrarFalha();
            logger.registrarErro("Erro de runtime ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
            repository.tratarFalha(caminho);
        } catch (Exception e) {
            relatorio.registrarFalha();
            logger.registrarErro("Erro inesperado (Exception) ao processar arquivo: " + caminho.getFileName() + " - " + e.getMessage());
            repository.tratarFalha(caminho);
        }
    }

    private ILeitor selecionarLeitor(Path caminho) {
        String nomeArquivo = caminho.getFileName().toString().toLowerCase();
        if (nomeArquivo.endsWith(".txt")) {
            logger.registrarSucesso("Leitor de Agência selecionado para: " + nomeArquivo);
            return leitorAgencia;
        } else if (nomeArquivo.endsWith(".bin")) {
            logger.registrarSucesso("Leitor de Caixa selecionado para: " + nomeArquivo);
            return leitorCaixa;
        } else {
            logger.registrarErro("Nenhum leitor compatível encontrado para: " + nomeArquivo);
            repository.tratarFalha(caminho);
            throw new FormatoArquivoInvalidoException("Formato de arquivo não suportado: " + nomeArquivo);
        }
    }
}