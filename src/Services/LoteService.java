package Services;

import Contracts.ILeitor;
import Contracts.ILoteService;
import Models.Lote;
import Models.Transacao;

import java.util.HashMap;
import java.util.Map;

public class LoteService implements ILoteService {
    private ILeitor leitor;
    private Map<String, Lote> lotes = new HashMap<>();

    public LoteService(ILeitor leitor, ILeitor leitorCaixa) {
        this.leitor = leitor;
    }
    @Override
    public void adicionarTransacao(Transacao transacao) {
        String chave = transacao.getEstado() + "-" + transacao.getNumeroFilial();
        if(!lotes.containsKey(chave)){
            Lote novoLote = new Lote(transacao.getEstado(), transacao.getNumeroFilial());
            lotes.put(chave, novoLote);
        }
        lotes.get(chave).adicionarTransacao(transacao);
    }
}
