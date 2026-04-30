package Services;

import Models.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AmbienteService {
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
                System.err.println("[ERRO] Não foi possível criar a pasta " + p + ": " + e.getMessage());
            }
        }
    }
}

