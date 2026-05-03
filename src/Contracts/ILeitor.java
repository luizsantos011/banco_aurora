package Contracts;

import Models.Transacao;
import java.nio.file.Path;

public interface ILeitor {
    Transacao lerArquivo(Path caminho);
}
