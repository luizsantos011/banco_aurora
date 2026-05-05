package Controller;

import Services.*;
import Contracts.*;
import Models.*;
import Repository.ArquivoRepository;

import java.io.IOException;
import java.nio.file.*;

public class SistemaController {
    private final AmbienteService ambienteService;
    private final ILogger logger;
    private final RelatorioFinal relatorio;
    private final ILoteService loteService;
    private final IProcessadorService processadorService;
    private final ArquivoRepository arquivoRepository;

    public SistemaController() {
        this.logger = new LogService();
        this.relatorio = new RelatorioFinal();
        this.loteService = new LoteService();
        this.ambienteService = new AmbienteService(logger);
        this.arquivoRepository = new ArquivoRepository(logger);
        ILeitor leitorAgencia = new LeitorAgencia(logger);
        ILeitor leitorCaixa = new LeitorCaixa(logger);
        this.processadorService = new ProcessadorService(leitorAgencia, leitorCaixa, loteService, logger, arquivoRepository, relatorio);
    }

    public void iniciarSistema() {
        ambienteService.inicializarSistema();
        arquivoRepository.limparQuarentena();
        logger.registrarSucesso("Setup concluído. Monitorando diretório de entrada.");
        monitorarDiretorio(PathConfig.ENTRADA_AGENCIAS);
        monitorarDiretorio(PathConfig.ENTRADA_CAIXAS);}

    private void monitorarDiretorio(Path caminho) {
        try(WatchService ws = FileSystems.getDefault().newWatchService()) {
            caminho.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey chave = ws.take();
                for (WatchEvent<?> evento : chave.pollEvents()) {
                    Path nomeArquivo = (Path) evento.context();
                    Path caminhoCompleto = caminho.resolve(nomeArquivo);
                    if(Files.isRegularFile(caminhoCompleto)) {
                        logger.registrarSucesso("Evento detectado: Novo arquivo identificado em " + nomeArquivo);
                        processadorService.processarArquivo(caminhoCompleto);
                    }
                }
                if(!chave.reset()) {
                    logger.registrarErro("Falha ao resetar WatchKey. Encerrando monitoramento.");
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.registrarErro("Erro crítico no monitoramento: " + e.getMessage());
        }
    }
}