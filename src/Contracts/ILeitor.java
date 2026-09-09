package Contracts;

import Models.Transacao;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ILeitor {
    List<Transacao> lerArquivo(Path caminho) throws IOException;
}