package Services;

import Models.Transacao;
import java.math.BigDecimal;
import java.util.function.Predicate;

public class RegraTransacao {
    public static Predicate<Transacao> valorAbaixoDe(BigDecimal limite) {
        return t -> t.getValor().compareTo(limite) <= 0;
    }

    public static Predicate<Transacao> todasValidas() {
        return t -> true;
    }
}