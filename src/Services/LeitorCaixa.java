package Services;

import Contracts.ILeitor;
import Contracts.ILogger;
import Models.Lote;
import Models.Transacao;
import Exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class LeitorCaixa implements ILeitor {
    private final ILogger logger;

    public LeitorCaixa(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public Lote lerArquivo(Path caminho) {
        Lote lote = null;

        try (FileChannel canal = FileChannel.open(caminho, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(64);

            if (canal.read(buffer) != -1) {
                buffer.flip();

                byte[] bId = new byte[7];
                buffer.get(bId);
                String idLote = new String(bId).trim();

                BigDecimal valorHeader = BigDecimal.valueOf(buffer.getDouble());

                lote = new Lote(Lote.Estado.SE, 1, LocalDateTime.now());
                buffer.clear();
            } else {
                throw new OperacaoInvalidaException("Arquivo binário vazio ou sem cabeçalho.");
            }

            while (canal.read(buffer) != -1) {
                buffer.flip();
                if (buffer.remaining() >= 48) {
                    byte[] bOrigem = new byte[20];
                    buffer.get(bOrigem);
                    String origem = new String(bOrigem).trim();

                    byte[] bDestino = new byte[20];
                    buffer.get(bDestino);
                    String destino = new String(bDestino).trim();

                    BigDecimal valor = BigDecimal.valueOf(buffer.getDouble());

                    if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ValorInvalidoException("Valor inválido encontrado no corpo do arquivo binário.");
                    }

                    lote.adicionarTransacao(new Transacao(origem, destino, valor));
                }
                buffer.clear();
            }

            return lote;

        } catch (IOException e) {
            throw new RuntimeException("Falha técnica no acesso ao arquivo binário", e);
        }
    }
}
