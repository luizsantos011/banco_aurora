package Services;

import Contracts.ILeitor;
import Contracts.ILogger;
import Models.Lote;
import Models.Transacao;
import Exceptions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeitorAgencia implements ILeitor {
    private final ILogger logger;

    public LeitorAgencia(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public Lote lerArquivo(Path caminho) {
        List<Transacao> transacoes = new ArrayList<>();
        BigDecimal somaTotal = BigDecimal.ZERO;

        try (BufferedReader br = Files.newBufferedReader(caminho)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] partes = linha.split(";");
                if (partes.length != 3) {
                    throw new OperacaoInvalidaException("Formato de colunas inválido: " + linha);
                }

                String origem = partes[0].trim();
                String destino = partes[1].trim();

                BigDecimal valor;
                try {
                    valor = new BigDecimal(partes[2].trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new ValorInvalidoException("Valor numérico inválido na linha: " + linha);
                }

                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ValorInvalidoException("Valor deve ser positivo. Linha: " + linha);
                }

                transacoes.add(new Transacao(origem, destino, valor));
                somaTotal = somaTotal.add(valor);
            }

            if (transacoes.isEmpty()) {
                throw new OperacaoInvalidaException("O arquivo não contém transações válidas.");
            }

            Lote loteFinal = new Lote(Lote.Estado.SE,2, LocalDateTime.now());
            for (Transacao t : transacoes) {
                loteFinal.adicionarTransacao(t);
            }

            return loteFinal;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

