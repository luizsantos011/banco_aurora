package Contracts;

import Models.Transacao;
import java.nio.file.Path;
import java.util.List;

public interface ILeitor {
    List<Transacao> lerArquivo(Path caminho);
}
