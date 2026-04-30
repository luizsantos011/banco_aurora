package Models;

import Exceptions.ValorInvalidoException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class RelatorioFinal {
    private int totalArquivos;
    private int transacoesSucesso;
    private int transacoesFalha;
    private BigDecimal volumeFinanceiro;

    public RelatorioFinal() {
        this.totalArquivos = 0;
        this.transacoesSucesso = 0;
        this.transacoesFalha = 0;
        this.volumeFinanceiro = BigDecimal.ZERO;
    }

    public void incrementarArquivos() {
        this.totalArquivos++;
    }

    public void registrarSucesso(BigDecimal valor) {
        validarValor(valor);
        this.transacoesSucesso++;
        this.volumeFinanceiro = this.volumeFinanceiro.add(valor);
    }

    public void registrarFalha() {
        this.transacoesFalha++;
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null ) {
            throw new NullPointerException("Valor para relatório não pode ser nulo.");
        }
        if(valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValorInvalidoException("Valor para relatório não pode ser negativo.");
        }
    }

    public void exibirResumo() {
        NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        System.out.println("\n=== RESUMO DO PROCESSAMENTO (FECHAMENTO) ===");
        System.out.println("Arquivos analisados: " + totalArquivos);
        System.out.println("Transações confirmadas: " + transacoesSucesso);
        System.out.println("Transações rejeitadas: " + transacoesFalha);
        System.out.println("Volume total movimentado: " + moeda.format(volumeFinanceiro));
        System.out.println("============================================\n");
    }

    public int getTotalArquivos() { return totalArquivos; }
    public int getTransacoesSucesso() { return transacoesSucesso; }
    public int getTransacoesFalha() { return transacoesFalha; }
    public BigDecimal getVolumeFinanceiro() { return volumeFinanceiro; }
}