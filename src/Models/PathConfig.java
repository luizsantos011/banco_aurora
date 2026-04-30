package Models;

import java.nio.file.Path;

public class PathConfig {
    public static final Path RAIZ = Path.of("banco_aurora");
    //Pastas de entrada
    public static final Path ENTRADA_AGENCIAS = RAIZ.resolve("entrada").resolve("agencias");
    public static final Path ENTRADA_CAIXAS = RAIZ.resolve("entrada").resolve("caixas");
    public static final Path ENTRADA_RETORNOS = RAIZ.resolve("entrada").resolve("retornos");
    //Pastas de processamento
    public static final Path PROCESSANDO =RAIZ.resolve("processando");
    public static final Path PROCESSADOS = RAIZ.resolve("processados");
    public static final Path QUARENTENA = RAIZ.resolve("quarentena");
    public static final Path REPROCESSAR = RAIZ.resolve("reprocessar");
    //Pastas de auditoria
    public static final Path LOGS = RAIZ.resolve("logs");
    public static final Path BACKUP = RAIZ.resolve("backup");
}

