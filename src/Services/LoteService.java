package Services;

import Contracts.ILoteService;
import Models.Lote;
import Models.Transacao;
import Exceptions.ValorInvalidoException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class LoteService implements ILoteService {
    private final Map<String, Lote> lotes = new HashMap<>();
    private final Predicate<Transacao> regraValidacao;

    public LoteService(Predicate<Transacao> regraValidacao) {
        this.regraValidacao = regraValidacao;
    }

    public LoteService() {
        this(RegraTransacao.todasValidas());
    }

    @Override
    public void adicionarTransacao(Transacao transacao) {
        if (!regraValidacao.test(transacao)) {
            throw new ValorInvalidoException("Transação rejeitada pela regra de negócio (Lambda): " + transacao.getValor());
        }

        String chave = transacao.getEstado() + "-" + transacao.getNumeroFilial();
        if (!lotes.containsKey(chave)) {
            Lote novoLote = new Lote(transacao.getEstado(), transacao.getNumeroFilial());
            lotes.put(chave, novoLote);
        }
        lotes.get(chave).adicionarTransacao(transacao);
    }
}