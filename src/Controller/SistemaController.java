package Controller;

import Services.*;
import Contracts.*;
import Models.*;
import Repository.ArquivoRepository;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.List;

public class SistemaController {
    private final AmbienteService ambienteService;
    private final ILogger logger;
    private final RelatorioFinal relatorio;
    private final IProcessadorService processadorService;

    public SistemaController() {
        this.logger = new LogService();
        this.ambienteService = new AmbienteService();
        this.relatorio = new RelatorioFinal();

        IArquivoRepository repository = new ArquivoRepository();
        ILeitor leitorAgencia = new LeitorAgencia(logger);
        ILeitor leitorCaixa = new LeitorCaixa(logger);

        this.processadorService = new ProcessadorService(
                leitorAgencia, leitorCaixa, logger, repository, this.relatorio
        );
    }

    public void iniciarSistema() {
        try {
            ambienteService.inicializarSistema();
            executarLoopPrincipal();
        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] Falha na inicialização: " + e.getMessage());
        }
    }

    private void executarLoopPrincipal() {
        Scanner scanner = new Scanner(System.in);
        int opcao = -1;

        while (opcao != 0) {
            exibirMenu();
            try {
                String entrada = scanner.nextLine();
                opcao = entrada.equalsIgnoreCase("sair") ? 0 : Integer.parseInt(entrada);
                processarEscolha(opcao, scanner);
            } catch (NumberFormatException e) {
                System.out.println("\n[AVISO] Entrada inválida. Digite um número ou 'sair'.");
            } catch (Exception e) {
                System.out.println("\n[AVISO] Ocorreu um erro inesperado: " + e.getMessage());
            }
        }
    }

    private void exibirMenu() {
        System.out.println("\n================================================");
        System.out.println("          BANCO AURORA - PAINEL DE CONTROLE");
        System.out.println("================================================");
        System.out.println("1 - [GERAR] Massa de Dados (Cenário Completo)");
        System.out.println("2 - [PROCESSAR] Varredura de Entradas (Normal)");
        System.out.println("3 - [RETORNOS] Reprocessar Arquivos Corrigidos");
        System.out.println("4 - [AUDITORIA] Visualizar Log de Eventos");
        System.out.println("5 - [BALANÇO] Relatório Consolidado");
        System.out.println("0 - Sair");
        System.out.print("\nComando > ");
    }

    private void processarEscolha(int opcao, Scanner scanner) {
        switch (opcao) {
            case 1 -> integrarGerador();
            case 2 -> {
                logger.registrarSucesso("\n[OPERAÇÃO] INICIANDO VARREDURA DE ENTRADAS PADRÃO");
                processadorService.executar(PathConfig.ENTRADA_AGENCIAS);
                processadorService.executar(PathConfig.ENTRADA_CAIXAS);
                logger.registrarSucesso("[OPERAÇÃO] VARREDURA DE ENTRADA CONCLUÍDA\n");
            }
            case 3 -> fluxoRetornos(scanner);
            case 4 -> visualizarLogs();
            case 5 -> exibirRelatorioBonito();
            case 0 -> System.out.println("\n[SISTEMA] Encerrando atividades...");
            default -> System.out.println("Opção inválida!");
        }
    }

    private void fluxoRetornos(Scanner scanner) {
        System.out.println("\n--- MÓDULO DE REPROCESSAMENTO ---");
        System.out.println("A - Simular correção humana (Mover p/ Retornos)");
        System.out.println("B - Executar Processamento de Retornos");
        System.out.print("Escolha: ");
        String sub = scanner.nextLine().toUpperCase();

        if (sub.equals("A")) {
            simularCorrecaoHumana();
        } else if (sub.equals("B")) {
            logger.registrarSucesso("\n[OPERAÇÃO] INICIANDO REPROCESSAMENTO DE RETORNOS");
            processadorService.executar(PathConfig.ENTRADA_RETORNOS);
            logger.registrarSucesso("[OPERAÇÃO] REPROCESSAMENTO CONCLUÍDO\n");
        }
    }

    private void simularCorrecaoHumana() {
        System.out.println("[SISTEMA] Corrigindo LOTE_FRAUDE e AGENCIA_ERRO...");
        try {
            gerarBinarioRetorno("LOTE_RECUPERADO.dat", "SC-1066", 100.0);
            Files.writeString(PathConfig.ENTRADA_RETORNOS.resolve("AGENCIA_RECUPERADA.txt"), "001;100;250.00");
            logger.registrarSucesso("[SIMULAÇÃO] Arquivos corrigidos e movidos para RETORNOS.");
        } catch (IOException e) {
            logger.registrarErro("[SIMULAÇÃO] Falha de I/O: " + e.getMessage());
        }
    }

    private void integrarGerador() {
        System.out.println("\n[SISTEMA] Injetando Cenário de Teste Completo...");
        try {
            gerarBinario("LOTE_OK.dat", "SC-1001", 100.0, true);
            Files.writeString(PathConfig.ENTRADA_AGENCIAS.resolve("AGENCIA_OK.txt"), "001;102;500.00");
            gerarBinario("LOTE_FRAUDE.dat", "SC-1066", 100.0, false);
            Files.writeString(PathConfig.ENTRADA_AGENCIAS.resolve("AGENCIA_ERRO.txt"), "001;102;VALOR_INVALIDO");
            Files.writeString(PathConfig.ENTRADA_CAIXAS.resolve("DESCONHECIDO.exe"), "Conteúdo malicioso");
            Files.writeString(PathConfig.ENTRADA_AGENCIAS.resolve("FOTO_CLIENTE.jpg"), "Dados binários");
            logger.registrarSucesso("[SISTEMA] Massa de dados injetada com sucesso.");
        } catch (IOException e) {
            logger.registrarErro("[SISTEMA] Erro de disco: " + e.getMessage());
        }
    }

    private void gerarBinario(String nome, String id, double valor, boolean ehBinario) throws IOException {
        Path p = PathConfig.ENTRADA_CAIXAS.resolve(nome);
        escreverArquivo(p, id, valor, ehBinario ? valor : valor - 10.0);
    }

    private void gerarBinarioRetorno(String nome, String id, double valor) throws IOException {
        Path p = PathConfig.ENTRADA_RETORNOS.resolve(nome);
        escreverArquivo(p, id, valor, valor);
    }

    private void escreverArquivo(Path p, String id, double valor, double soma) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(p.toFile()))) {
            dos.writeBytes(String.format("%-7s", id));
            dos.writeDouble(valor);
            dos.write(new byte[49]);
            dos.writeBytes(String.format("%-20s", "ORIGEM"));
            dos.writeBytes(String.format("%-20s", "DESTINO"));
            dos.writeDouble(soma);
        }
    }

    private void exibirRelatorioBonito() {
        System.out.println("\n================================================");
        System.out.println("           BALANÇO FINANCEIRO CONSOLIDADO");
        System.out.println("================================================");
        relatorio.exibirResumo();
        System.out.println("------------------------------------------------");
        System.out.println("Auditado por: Sistema de Auditoria Aurora v1.0");
        System.out.println("================================================");
    }

    private void visualizarLogs() {
        System.out.println("\n--- TRILHA DE AUDITORIA COMPLETA ---");
        Path logPath = PathConfig.LOGS.resolve("auditoria.log");
        try {
            if (Files.exists(logPath)) {
                List<String> linhas = Files.readAllLines(logPath);
                int limite = Math.max(0, linhas.size() - 50);
                for (int i = limite; i < linhas.size(); i++) {
                    System.out.println("  " + linhas.get(i));
                }
            } else {
                System.out.println("[!] Sem registros de auditoria.");
            }
        } catch (IOException e) {
            System.out.println("[ERRO] Falha de leitura do log.");
        }
    }
}

