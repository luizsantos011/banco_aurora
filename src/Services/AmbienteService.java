package Services;

import Contracts.ILogger;
import Models.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AmbienteService {
    private final ILogger logger;

    public AmbienteService(ILogger logger) {
        this.logger = logger;
    }

    public void inicializarSistema() {
        Path[] caminhos = {
                PathConfig.RAIZ,
                PathConfig.ENTRADA_AGENCIAS,
                PathConfig.ENTRADA_CAIXAS,
                PathConfig.ENTRADA_RETORNOS,
                PathConfig.PROCESSANDO,
                PathConfig.PROCESSADOS,
                PathConfig.QUARENTENA,
                PathConfig.REPROCESSAR,
                PathConfig.LOGS,
                PathConfig.BACKUP
        };

        for (Path p : caminhos) {
            try {
                if (Files.notExists(p)) {
                    Files.createDirectories(p);
                    System.out.println("[OK] Pasta criada: " + p.getFileName());
                }
            } catch (IOException e) {
                logger.registrarErro("Falha ao criar pasta " + p.getFileName() + ": " + e.getMessage());
            }
        }
        logger.registrarSucesso("Iniciando setup do sistema Aurora.");
    }
}

