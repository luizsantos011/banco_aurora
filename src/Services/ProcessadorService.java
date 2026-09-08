package Services;

import Contracts.*;
import Models.*;
import Exceptions.*;
import java.io.IOException;
import java.nio.file.Path;
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
        try {
            logger.registrarSucesso("Iniciando processamento do arquivo: " + caminho.getFileName());

            ILeitor leitor = selecionarLeitor(caminho);
            List<Transacao> transacoes = leitor.lerArquivo(caminho);

            arquivoImportado = repository.preparar(caminho);
            repository.finalizarComSucesso(arquivoImportado);

            for (Transacao t : transacoes) {
                iLoteService.adicionarTransacao(t);
                relatorio.registrarSucesso(t.getValor());
            }

            relatorio.incrementarArquivos();
            logger.registrarSucesso("Arquivo processado e finalizado com sucesso: " + caminho.getFileName());

        } catch (IOException e) {
            tratarErro(e, "Erro de E/S ao processar arquivo: ", caminho, arquivoImportado);
        } catch (FormatoArquivoInvalidoException | ValorInvalidoException | OperacaoInvalidaException e) {
            tratarErro(e, "Erro de validação de negócio no arquivo: ", caminho, arquivoImportado);
        } catch (RuntimeException e) {
            tratarErro(e, "Erro de runtime ao processar arquivo: ", caminho, arquivoImportado);
        } catch (Exception e) {
            tratarErro(e, "Erro inesperado (Exception) ao processar arquivo: ", caminho, arquivoImportado);
        }
    }

    private void tratarErro(Exception e, String mensagemLog, Path caminhoOriginal, ArquivoImportado arquivoImportado) {
        relatorio.registrarFalha();
        logger.registrarErro(mensagemLog + caminhoOriginal.getFileName() + " - " + e.getMessage());
        Path caminhoAtual = (arquivoImportado != null) ? arquivoImportado.getLocalizacao() : caminhoOriginal;
        repository.tratarFalha(caminhoAtual);
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