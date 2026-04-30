package Repository;

import Contracts.IArquivoRepository;
import Models.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ArquivoRepository implements IArquivoRepository {

    @Override
    public ArquivoImportado preparar(Path caminho) throws IOException {
        ArquivoImportado arquivo = new ArquivoImportado(
                caminho.getFileName().toString(),
                Files.size(caminho),
                caminho
        );

        Path backupDestino = PathConfig.BACKUP.resolve(arquivo.getNome());
        Files.copy(caminho, backupDestino, StandardCopyOption.REPLACE_EXISTING);

        Path destinoProcessamento = PathConfig.PROCESSANDO.resolve(arquivo.getNome());
        Files.move(caminho, destinoProcessamento, StandardCopyOption.REPLACE_EXISTING);

        return new ArquivoImportado(arquivo.getNome(), arquivo.getTamanho(), destinoProcessamento);
    }

    @Override
    public void finalizarComSucesso(ArquivoImportado arquivo) throws IOException {
        Path destino = PathConfig.PROCESSADOS.resolve(arquivo.getNome());
        Files.move(arquivo.getLocalizacao(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void finalizarComErro(ArquivoImportado arquivo) throws IOException {
        Path destino = PathConfig.REPROCESSAR.resolve(arquivo.getNome());
        Files.move(arquivo.getLocalizacao(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void tratarFalhaCritica(Path caminho) {
        try {
            Path destino = PathConfig.QUARENTENA.resolve(caminho.getFileName());
            Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erro ao isolar arquivo: " + e.getMessage());
        }
    }
}