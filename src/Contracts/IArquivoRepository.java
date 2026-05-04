package Contracts;

import Models.ArquivoImportado;

import java.io.IOException;
import java.nio.file.Path;

public interface IArquivoRepository {
    ArquivoImportado preparar(Path caminho) throws IOException;
    void finalizarComSucesso(ArquivoImportado arquivo) throws IOException;
    void tratarFalha(Path caminho);
}
