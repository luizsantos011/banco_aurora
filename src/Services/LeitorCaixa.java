package Services;

import Contracts.ILeitor;
import Contracts.ILogger;
import Models.Lote;
import Models.Lote.Estado;
import Models.Transacao;
import Exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LeitorCaixa implements ILeitor {
    private final ILogger logger;

    public LeitorCaixa(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public Transacao lerArquivo(Path caminho) {
        Transacao transacao = null;

        try (FileChannel canal = FileChannel.open(caminho, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(64);

            while (canal.read(buffer) != -1) {
                buffer.flip();
                if (buffer.remaining() >= 55) {
                    byte[] bEstado = new byte[3];
                    buffer.get(bEstado);
                    Estado estado = Estado.valueOf(new String(bEstado).trim());

                    byte[] bNumeroFilial = new byte[4];
                    buffer.get(bNumeroFilial);
                    int numeroFilial = Integer.parseInt(new String(bNumeroFilial).trim());

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
                    transacao = new Transacao(estado, numeroFilial, origem, destino, valor);
                }
                buffer.clear();
            }
            if (transacao == null) throw new OperacaoInvalidaException("Arquivo binário vazio ou sem transações válidas.");
            return transacao;
        } catch (IOException e) {
            throw new RuntimeException("Falha técnica no acesso ao arquivo binário", e);
        }
    }
}
