package Services;

import Contracts.ILeitor;
import Contracts.ILogger;
import Models.Lote.Estado;
import Models.Transacao;
import Exceptions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class LeitorCaixa implements ILeitor {
    private static final int TAMANHO_PAYLOAD_ESPERADO = 55;
    private final ILogger logger;

    public LeitorCaixa(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public List<Transacao> lerArquivo(Path caminho) {
        List<Transacao> transacoes = new ArrayList<>();

        try (FileChannel canal = FileChannel.open(caminho, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (canal.read(buffer) != -1) {
                buffer.flip();
                while (buffer.remaining() >= 4) {
                    buffer.mark();
                    int tamanhoPayLoad = buffer.getInt();

                    if (tamanhoPayLoad != TAMANHO_PAYLOAD_ESPERADO) {
                        throw new FormatoArquivoInvalidoException(
                                "Tamanho de payload inválido no arquivo binário: " + tamanhoPayLoad + " bytes (esperado: 55)"
                        );
                    }

                    if (buffer.remaining() < tamanhoPayLoad) {
                        buffer.reset();
                        break;
                    }

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
                    Transacao transacao = new Transacao(estado, numeroFilial, origem, destino, valor);
                    transacoes.add(transacao);
                }
                buffer.compact();
            }
            if (transacoes.isEmpty()) throw new OperacaoInvalidaException("Arquivo binário vazio ou sem transações válidas.");
            return transacoes;
        } catch (IOException e) {
            throw new RuntimeException("Falha técnica no acesso ao arquivo binário", e);
        }
    }
}