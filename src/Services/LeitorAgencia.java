package Services;

import Contracts.ILeitor;
import Contracts.ILogger;
import Models.Lote.*;
import Models.*;
import Exceptions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

public class LeitorAgencia implements ILeitor {
    private final ILogger logger;

    public LeitorAgencia(ILogger logger) {
        this.logger = logger;
    }

    @Override
    public Transacao lerArquivo(Path caminho) {
        Transacao transacao = null;

        try (BufferedReader br = Files.newBufferedReader(caminho)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] partes = linha.split(";");
                if (partes.length != 4) {
                    throw new OperacaoInvalidaException("Formato de colunas inválido: " + linha);
                }

                String filial = partes[0];
                String[] partesFilial = filial.split("-");
                if (partesFilial.length != 2) {
                    throw new OperacaoInvalidaException("Formato de filial inválido: " + filial);
                }
                Estado estado = Estado.valueOf(partesFilial[0]);
                int numeroFilial = Integer.parseInt(partesFilial[1]);
                String origem = partes[1].trim();
                String destino = partes[2].trim();

                BigDecimal valor;
                try {
                    valor = new BigDecimal(partes[3].trim().replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new ValorInvalidoException("Valor numérico inválido na linha: " + linha);
                }
                transacao = new Transacao(estado,numeroFilial, origem, destino, valor);
            }

            if(transacao == null) throw new OperacaoInvalidaException("Arquivo de texto vazio ou sem transações válidas.");

            return transacao;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

