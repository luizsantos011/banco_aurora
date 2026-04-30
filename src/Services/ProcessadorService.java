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
    private final ILogger logger;
    private final IArquivoRepository repository;
    private final RelatorioFinal relatorio;
    private final List<Transacao> todasAsTransacoes;

    public ProcessadorService(ILeitor leitorAgencia, ILeitor leitorCaixa, ILogger logger,
                              IArquivoRepository repository, RelatorioFinal relatorio) {
        this.leitorAgencia = leitorAgencia;
        this.leitorCaixa = leitorCaixa;
        this.logger = logger;
        this.repository = repository;
        this.relatorio = relatorio;
        this.todasAsTransacoes = new ArrayList<>();
    }

    @Override
    public void executar(Path pastaOrigem) {
        try {
            if (Files.notExists(pastaOrigem)) return;

            List<Path> arquivos = Files.list(pastaOrigem)
                    .filter(p -> !Files.isDirectory(p))
                    .toList();

            for (Path arquivo : arquivos) {
                processarArquivo(arquivo);
            }
        } catch (IOException e) {
            logger.registrarErro("Falha crítica de I/O ao acessar pasta: " + pastaOrigem.getFileName());
        }
    }

    private void processarArquivo(Path caminhoOriginal) {
        ArquivoImportado arquivo = null;
        String nomeBase = caminhoOriginal.getFileName().toString();
        logger.registrarSucesso("\n>>> ANALISANDO: " + nomeBase);
        String extensao = nomeBase.toLowerCase();

        try {

            if (!extensao.endsWith(".dat") && !extensao.endsWith(".txt")) {
                repository.tratarFalhaCritica(caminhoOriginal);
                logger.registrarErro("   [TRIAGEM] Bloqueado: Extensão não permitida.");
                logger.registrarErro(">>> STATUS: QUARENTENA - " + nomeBase);
                return;
            }

            arquivo = repository.preparar(caminhoOriginal);
            relatorio.incrementarArquivos();

            logger.registrarSucesso("   [PARSER] Iniciando leitura do conteúdo...");
            Lote lote = selecionarLeitorEParsar(arquivo);

            if (lote != null && !lote.getTransacoes().isEmpty()) {
                executarProcessamentoSucesso(lote, arquivo);
                logger.registrarSucesso(">>> STATUS: SUCESSO - " + nomeBase);
            } else {
                throw new OperacaoInvalidaException("Lote sem transações processáveis.");
            }

        } catch (IOException e) {
            relatorio.registrarFalha();
            logger.registrarErro("   [SISTEMA] Erro de I/O: " + e.getMessage());
            tratarErroFluxo(arquivo, caminhoOriginal, e.getMessage());
            logger.registrarErro(">>> STATUS: QUARENTENA - " + nomeBase);

        } catch (IdInvalidoException | ValorInvalidoException | DataInvalidaException |
                 ContaInvalidaException | OperacaoInvalidaException e) {
            relatorio.registrarFalha();
            try {
                repository.finalizarComErro(arquivo);
            } catch (IOException ioEx) {
                logger.registrarErro("   [SISTEMA] Falha ao mover para reprocessar.");
            }
            logger.registrarErro("   [NEGOCIO] Rejeitado: " + e.getMessage());
            logger.registrarErro(">>> STATUS: REPROCESSAR - " + nomeBase);

        } catch (Exception e) {
            relatorio.registrarFalha();
            logger.registrarErro("   [CRÍTICO] Falha inesperada: " + e.getClass().getSimpleName());
            tratarErroFluxo(arquivo, caminhoOriginal, e.getMessage());
            logger.registrarErro(">>> STATUS: QUARENTENA - " + nomeBase);
        }
    }

    private void executarProcessamentoSucesso(Lote lote, ArquivoImportado arquivo) throws IOException {
        logger.registrarSucesso("   [NEGOCIO] Lote Identificado: " + lote.getIdLote());

        for (Transacao t : lote.getTransacoes()) {
            this.todasAsTransacoes.add(t);
            relatorio.registrarSucesso(t.getValor());
            logger.registrarSucesso("      [TX] " + t.getOrigem() + " -> " + t.getDestino() + " | R$ " + t.getValor());
        }

        repository.finalizarComSucesso(arquivo);
    }

    private Lote selecionarLeitorEParsar(ArquivoImportado arquivo) {
        String nome = arquivo.getNome().toLowerCase();
        if (nome.endsWith(".txt")) {
            return leitorAgencia.lerArquivo(arquivo.getLocalizacao());
        } else if (nome.endsWith(".dat")) {
            return leitorCaixa.lerArquivo(arquivo.getLocalizacao());
        }
        return null;
    }

    private void tratarErroFluxo(ArquivoImportado arquivo, Path caminhoOriginal, String erro) {
        if (arquivo != null) {
            repository.tratarFalhaCritica(arquivo.getLocalizacao());
        } else {
            repository.tratarFalhaCritica(caminhoOriginal);
        }
    }

    public List<Transacao> getTodasTransacoes() {
        return todasAsTransacoes;
    }
}

