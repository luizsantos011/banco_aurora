package Contracts;

import Models.Lote;
import Models.Transacao;
import java.nio.file.Path;
import java.util.List;

public interface ILeitor {
    Lote lerArquivo(Path caminho);
}
