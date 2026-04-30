package Models;

import Exceptions.ContaInvalidaException;
import Exceptions.ValorInvalidoException;

import java.math.BigDecimal;

public class Conta {
    private final String numeroConta;
    private final int agencia;
    private BigDecimal saldo;

    public Conta(String numeroConta, int agencia, BigDecimal saldo){
        validarNumeroConta(numeroConta);
        validarAgencia(agencia);
        validarSaldo(saldo);
        this.numeroConta = numeroConta;
        this.agencia = agencia;
        this.saldo = saldo;
    }

    private void validarNumeroConta(String numeroConta){
        if(numeroConta == null){
            throw new NullPointerException("Número da conta não pode ser nulo.");
        }
        if(numeroConta.isBlank()){
            throw new IllegalArgumentException("Número da conta não pode ser vazio.");
        }
        if (!numeroConta.matches("\\d{8}-\\d")){
            throw new ContaInvalidaException("Número da conta deve estar no formato XXXXXXXX-X");
        }
    }
    private void validarAgencia(int agencia){
        if(agencia < 1000 || agencia > 9999){
            throw new ContaInvalidaException("Agência deve ser um número de 4 dígitos.");
        }
    }
    private void validarSaldo(BigDecimal saldo){
        if(saldo == null){
            throw new NullPointerException("Saldo não pode ser nulo.");
        }
        if(saldo.compareTo(BigDecimal.ZERO) < 0){
            throw new ValorInvalidoException("Saldo não pode ser negativo.");
        }
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public int getAgencia() {
        return agencia;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
