package Repository;

import Contracts.IArquivoRepository;
import Contracts.ILogger;
import Models.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ArquivoRepository implements IArquivoRepository {
    private final ILogger logger;

    public ArquivoRepository(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public ArquivoImportado preparar(Path caminho) throws IOException {
        logger.registrarSucesso("Preparando arquivo para processamento: " + caminho.getFileName());

        ArquivoImportado arquivo = new ArquivoImportado(
                caminho.getFileName().toString(),
                Files.size(caminho),
                caminho
        );

        Path backupDestino = PathConfig.BACKUP.resolve(arquivo.getNome());
        Files.copy(caminho, backupDestino, StandardCopyOption.REPLACE_EXISTING);
        logger.registrarSucesso("Backup criado com sucesso em: " + backupDestino);

        Path destinoProcessamento = PathConfig.PROCESSANDO.resolve(arquivo.getNome());
        Files.move(caminho, destinoProcessamento, StandardCopyOption.REPLACE_EXISTING);
        logger.registrarSucesso("Arquivo movido para área de processamento: " + destinoProcessamento);

        return new ArquivoImportado(arquivo.getNome(), arquivo.getTamanho(), destinoProcessamento);
    }

    @Override
    public void tratarFalha(Path caminho) {
        try {
            if (!Files.isReadable(caminho)) {
                Path destino = PathConfig.REPROCESSAR.resolve(caminho.getFileName().toString());
                Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
                logger.registrarErro("Arquivo ilegível. Movido para REPROCESSAR: " + caminho.getFileName());
                return;
            }
            if (Files.size(caminho) == 0) {
                Path destino = PathConfig.QUARENTENA.resolve(caminho.getFileName().toString());
                Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
                logger.registrarErro("Arquivo vazio detectado. Movido para QUARENTENA: " + caminho.getFileName());
                return;
            }
            Path destino = PathConfig.QUARENTENA.resolve(caminho.getFileName().toString());
            Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
            logger.registrarErro("Falha no processamento. Movido para QUARENTENA por precaução: " + caminho.getFileName());
        } catch (IOException e) {
            logger.registrarErro("Erro crítico ao isolar arquivo: " + e.getMessage());
        }
    }

    public void finalizarComSucesso(ArquivoImportado arquivo) throws IOException {
        Path destino = PathConfig.PROCESSADOS.resolve(arquivo.getNome());
        Files.move(arquivo.getLocalizacao(), destino, StandardCopyOption.REPLACE_EXISTING);
        logger.registrarSucesso("Arquivo finalizado com sucesso. Movido para PROCESSADOS: " + arquivo.getNome());
    }

    public void limparQuarentena() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(PathConfig.QUARENTENA)) {
            int deletados = 0;
            for (Path arquivo : stream) {
                Files.delete(arquivo);
                deletados++;
            }
            if(deletados > 0) {
                logger.registrarSucesso("Limpeza da quarentena realizada. Arquivos removidos: " + deletados);
            }
        } catch (IOException e) {
            logger.registrarErro("Erro ao limpar quarentena: " + e.getMessage());
        }
    }
}