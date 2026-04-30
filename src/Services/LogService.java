package Services;

import Contracts.ILogger;
import Models.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LogService implements ILogger {

    public LogService() {}

    @Override
    public void registrarErro(String mensagem) {
        salvar(new RegistroAuditoria("ERRO", mensagem));
    }

    @Override
    public void registrarSucesso(String mensagem) {
        salvar(new RegistroAuditoria("SUCESSO", mensagem));
    }

    private void salvar(RegistroAuditoria registro) {
        String linha = registro.toString() + System.lineSeparator();

        try {
            Files.writeString(
                    PathConfig.LOGS.resolve("auditoria.log"),
                    linha,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.print(linha);

        } catch (IOException e) {
            System.err.println("ERRO CRÍTICO DE I/O: Não foi possível gravar no arquivo de auditoria.");
            System.err.println(linha);
        }
    }
}

