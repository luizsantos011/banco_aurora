package Services;

import Contracts.ILeitor;
import Contracts.ILote;
import Models.Lote;
import Models.Transacao;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LoteService implements ILote {
    private ILeitor leitor;
    private ILote lote;
    private final Map<String, Lote> lotesEmMemoria = new HashMap<>();

    public LoteService(ILeitor leitor, ILeitor leitorCaixa) {
        this.leitor = leitor;
    }
    @Override
    public void adicionarTransacao(Transacao transacao) {

    private void criarLote(){

        }
    }
}
