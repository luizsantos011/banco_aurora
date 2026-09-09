package Repository;

import Contracts.IArquivoRepository;
import Contracts.ILogger;
import Exceptions.OperacaoInvalidaException;
import Models.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class ArquivoRepository implements IArquivoRepository {
    private final ILogger logger;

    public ArquivoRepository(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public ArquivoImportado preparar(Path caminho) throws IOException {
        logger.registrarSucesso("Preparando arquivo para processamento: " + caminho.getFileName());

        validarEBloquearConcorrencia(caminho);

        String nomeUnico = obterNomeUnico(caminho);

        Path backupDestino = PathConfig.BACKUP.resolve(nomeUnico);
        executarBackupComTransferTo(caminho, backupDestino);

        Path destinoProcessamento = PathConfig.PROCESSANDO.resolve(nomeUnico);
        Files.move(caminho, destinoProcessamento, StandardCopyOption.REPLACE_EXISTING);
        logger.registrarSucesso("Arquivo movido para área de processamento: " + destinoProcessamento);

        return new ArquivoImportado(nomeUnico, Files.size(destinoProcessamento), destinoProcessamento);
    }

    private void validarEBloquearConcorrencia(Path caminho) throws IOException {
        try (FileChannel canal = FileChannel.open(caminho, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            FileLock lock = canal.tryLock();
            if (lock == null) {
                throw new IOException("Arquivo ja esta sendo processado por outra instancia: " + caminho.getFileName());
            }
            lock.release();
        }
    }

    private void executarBackupComTransferTo(Path origem, Path destino) throws IOException {
        try (FileChannel canalOrigem = FileChannel.open(origem, StandardOpenOption.READ);
             FileChannel canalDestino = FileChannel.open(destino, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            long pos = 0;
            long tamanho = canalOrigem.size();
            while (pos < tamanho) {
                pos += canalOrigem.transferTo(pos, tamanho - pos, canalDestino);
            }
            canalDestino.force(true);
        }
        logger.registrarSucesso("Backup (transferTo/force) criado em: " + destino);
    }

    @Override
    public void tratarFalha(Path caminho) {
        try {
            String nomeArquivo = caminho.getFileName().toString();
            if (!Files.isReadable(caminho)) {
                Path destino = PathConfig.REPROCESSAR.resolve(nomeArquivo);
                Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
                logger.registrarErro("Arquivo ilegivel. Movido para REPROCESSAR: " + nomeArquivo);
                return;
            }
            if (Files.size(caminho) == 0) {
                Path destino = PathConfig.QUARENTENA.resolve(nomeArquivo);
                Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
                logger.registrarErro("Arquivo vazio detectado. Movido para QUARENTENA: " + nomeArquivo);
                return;
            }
            Path destino = PathConfig.QUARENTENA.resolve(nomeArquivo);
            Files.move(caminho, destino, StandardCopyOption.REPLACE_EXISTING);
            logger.registrarErro("Falha no processamento. Movido para QUARENTENA: " + nomeArquivo);
        } catch (IOException e) {
            logger.registrarErro("Erro critico ao isolar arquivo: " + e.getMessage());
        }
    }

    @Override
    public void finalizarComSucesso(ArquivoImportado arquivo) throws IOException {
        validarDuplicidade(arquivo);
        Path destino = PathConfig.PROCESSADOS.resolve(arquivo.getNome());

        try {
            Files.move(arquivo.getLocalizacao(), destino, StandardCopyOption.REPLACE_EXISTING);
            logger.registrarSucesso("Arquivo finalizado com sucesso. Movido para PROCESSADOS: " + arquivo.getNome());
        } catch (IOException e) {
            throw new IOException("Falha ao mover arquivo para PROCESSADOS: " + e.getMessage(), e);
        }
    }

    private String obterNomeUnico(Path caminho) {
        Path pastaPai = caminho.getParent();
        String prefixo = (pastaPai != null) ? pastaPai.getFileName().toString().toLowerCase() + "_" : "";
        return prefixo + caminho.getFileName().toString();
    }

    private void validarDuplicidade(ArquivoImportado arquivo) {
        Path destino = PathConfig.PROCESSADOS.resolve(arquivo.getNome());
        if (Files.exists(destino)) {
            throw new OperacaoInvalidaException("Arquivo ja processado: " + arquivo.getNome());
        }
    }
}