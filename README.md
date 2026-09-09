# Banco Aurora

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Java NIO](https://img.shields.io/badge/NIO.2-FileChannel%20%26%20WatchService-007396?style=for-the-badge&logo=java&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Layered%20%2F%20Contract--based-brightgreen?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Conclu%C3%ADdo-blue?style=for-the-badge)

Sistema bancário de ingestão, validação, auditoria e processamento de
arquivos desenvolvido em Java, com foco no uso das APIs de I/O e NIO da
plataforma.

O sistema monitora diretórios de entrada de diferentes canais,
identifica o formato do arquivo, interpreta suas transações, mantém o
ciclo de vida dos arquivos entre áreas de processamento e registra as
operações em um log de auditoria.

O projeto foi desenvolvido como um trabalho acadêmico de Java NIO e
utiliza somente recursos da própria plataforma Java.

## Tecnologias e Recursos Nativos

| Categoria | Recursos Utilizados |
| :--- | :--- |
| **Linguagem & Core** | Java 17+, Collections, Expressões Lambda, `Predicate<T>` |
| **Entrada e Saída (NIO)** | `WatchService`, `FileChannel`, `ByteBuffer`, `FileLock`, `Path`, `Files` |
| **Parsing & Formato** | Protocolo binário, `BufferedReader`, `BigDecimal` |
| **Arquitetura** | Organização em camadas, contratos por interfaces, Repository, Models de domínio |

Não há frameworks ou bibliotecas externas.

## Estrutura do projeto

``` text
src/
├── Contracts/
│   ├── IArquivoRepository.java
│   ├── ILeitor.java
│   ├── ILogger.java
│   ├── ILoteService.java
│   └── IProcessadorService.java
├── Controller/
│   └── SistemaController.java
├── Exceptions/
├── Models/
│   ├── ArquivoImportado.java
│   ├── Lote.java
│   ├── PathConfig.java
│   ├── RegistroAuditoria.java
│   ├── RelatorioFinal.java
│   └── Transacao.java
├── Repository/
│   └── ArquivoRepository.java
├── Services/
│   ├── AmbienteService.java
│   ├── LeitorAgencia.java
│   ├── LeitorCaixa.java
│   ├── LogService.java
│   ├── LoteService.java
│   └── ProcessadorService.java
└── Main.java
```

A aplicação cria a seguinte estrutura de trabalho em tempo de execução:

``` text
banco_aurora/
├── entrada/
│   ├── agencias/
│   ├── caixas/
│   └── retornos/
├── processando/
├── processados/
├── quarentena/
├── reprocessar/
├── logs/
└── backup/
```

A pasta `banco_aurora/` é ignorada pelo Git, pois contém os arquivos
gerados durante a execução.

## Fluxo

O `SistemaController` inicializa o ambiente e registra um `WatchService`
nos diretórios de agências e caixas.

``` text
entrada/agencias/ ─┐
                   ├──> WatchService
entrada/caixas/ ──┘
                         |
                         v
                  ProcessadorService
                         |
                +--------+--------+
                |                 |
              .txt               .bin
                |                 |
                v                 v
        LeitorAgencia       LeitorCaixa
                |                 |
                +--------+--------+
                         |
                         v
                    Transações
                         |
                         v
                    LoteService
                         |
                         v
                 ciclo do arquivo
                         |
              +----------+----------+
              |                     |
           sucesso                 falha
              |                     |
              v                     v
        processados/       quarentena/reprocessar
```

O monitoramento e o processamento são sequenciais. O projeto não utiliza
threads de processamento ou `AsynchronousFileChannel`.

## Entrada de agências

Arquivos `.txt` são processados pelo `LeitorAgencia`.

Cada linha representa uma transação:

``` text
ESTADO-FILIAL;CONTA_ORIGEM;CONTA_DESTINO;VALOR
```

Exemplo:

``` text
SE-1;10001;20001;1500.50
BA-25;11111;22222;320.90
```

O leitor percorre todas as linhas não vazias e cria uma `Transacao` para
cada registro.

São verificadas, entre outras condições:

-   quantidade de campos;
-   formato de estado e filial;
-   estado reconhecido pelo domínio;
-   número da filial;
-   contas de origem e destino;
-   valor numérico;
-   valor positivo.

O valor aceita tanto ponto quanto vírgula como separador decimal.

## Entrada de caixas

Arquivos `.bin` são processados pelo `LeitorCaixa`.

O formato utiliza um cabeçalho de 4 bytes com o tamanho do payload. O
payload esperado possui 55 bytes:

``` text
3 bytes   estado
4 bytes   número da filial
20 bytes  conta de origem
20 bytes  conta de destino
8 bytes   valor
```

Representação:

``` text
[4 bytes: tamanho do payload]
[3 bytes: estado]
[4 bytes: filial]
[20 bytes: origem]
[20 bytes: destino]
[8 bytes: valor]
```

O leitor utiliza `FileChannel` e `ByteBuffer` para reconstruir mensagens
que podem chegar fragmentadas em diferentes leituras do canal.

O fluxo utiliza:

-   `allocate()` para criar o buffer;
-   `flip()` para alternar entre escrita e leitura;
-   `mark()` para marcar o início de uma mensagem;
-   `getInt()` para obter o tamanho do payload;
-   `remaining()` para verificar se a mensagem está completa;
-   `reset()` para retornar ao início de uma mensagem incompleta;
-   `compact()` para preservar os bytes ainda não processados.

O tamanho informado pelo cabeçalho é validado contra o protocolo
esperado. Se o arquivo terminar com uma mensagem incompleta, o
processamento é rejeitado.

## Domínio

### Transacao

`Transacao` representa uma movimentação entre duas contas.

O próprio objeto valida suas invariantes:

-   estado obrigatório;
-   filial entre 1 e 9999;
-   contas de origem e destino obrigatórias;
-   contas de origem e destino diferentes;
-   valor maior que zero.

A transação também recebe identificadores gerados com UUID e registra
sua data de criação.

### Lote

As transações são agrupadas por estado e número da filial.

Por exemplo:

``` text
SE + filial 25
```

gera o lote:

``` text
SE-0025
```

O lote mantém suas transações e acumula o valor total movimentado.

A coleção interna de transações não é exposta diretamente.
`getTransacoes()` retorna uma cópia imutável da lista.

### ArquivoImportado

Representa um arquivo que entrou no fluxo de processamento, mantendo
informações como:

-   nome;
-   tamanho;
-   data;
-   localização atual.

### RelatorioFinal

Mantém os indicadores do processamento:

-   quantidade de arquivos;
-   transações confirmadas;
-   transações rejeitadas;
-   volume financeiro processado.

O resumo pode ser exibido no console ao final do processamento.

## Ciclo de vida dos arquivos

### Preparação

Quando um arquivo é detectado, o `ArquivoRepository` verifica se ele
pode ser processado e prepara sua movimentação.

Arquivos provenientes de diretórios diferentes recebem um prefixo
baseado na origem:

``` text
agencias_arquivo.txt
caixas_arquivo.bin
```

Isso evita colisões simples de nomes entre os canais.

### Backup

Antes da movimentação para a área de processamento, é criado um backup
utilizando `FileChannel.transferTo()`.

Depois da transferência, `force(true)` é chamado no canal de destino
para solicitar a persistência das alterações.

### Controle de concorrência

Durante a preparação é feita uma tentativa de obter um `FileLock` com
`tryLock()`.

Se o arquivo já estiver bloqueado por outra operação, a preparação
falha.

O lock é utilizado como uma verificação de disponibilidade do arquivo;
ele não permanece mantido durante todo o processamento.

### Processando

Depois do backup, o arquivo é movido para:

``` text
banco_aurora/processando/
```

Essa área representa os arquivos que estão dentro do fluxo de
processamento.

### Processados

Arquivos concluídos são encaminhados para:

``` text
banco_aurora/processados/
```

Antes da finalização, o sistema verifica se já existe um arquivo com o
mesmo nome nessa área.

### Quarentena

Arquivos que apresentam falhas durante o processamento são isolados em:

``` text
banco_aurora/quarentena/
```

A quarentena não é apagada automaticamente quando o sistema reinicia.

### Reprocessar

Arquivos considerados ilegíveis são encaminhados para:

``` text
banco_aurora/reprocessar/
```

O projeto mantém essa área como parte do fluxo de tratamento, mas não
possui um mecanismo automático para reprocessamento.

## Auditoria

O `LogService` registra eventos de sucesso e erro em:

``` text
banco_aurora/logs/auditoria.log
```

A gravação utiliza `Files.writeString()` com:

``` java
StandardOpenOption.CREATE
StandardOpenOption.APPEND
```

Os mesmos registros também são exibidos no console.

Exemplo:

``` text
[09/09/2026 00:45:12] SUCESSO: Evento detectado: Novo arquivo identificado...
[09/09/2026 00:45:13] ERRO: Falha no processamento. Movido para QUARENTENA...
```

## Tratamento de falhas

O `ProcessadorService` centraliza o tratamento de erros de cada arquivo.

Há tratamento específico para:

-   `IOException`;
-   erros de formato;
-   erros de valor;
-   operações inválidas.

Também existem capturas genéricas para `RuntimeException` e `Exception`.

Esses tratamentos genéricos são intencionais: uma exceção inesperada em
um arquivo não deve derrubar o fluxo de monitoramento. O arquivo afetado
é isolado, o erro é registrado e o sistema continua disponível para os
próximos arquivos.

## Conceitos e Competências Demonstradas

Este projeto foi desenvolvido para consolidar fundamentos da plataforma Java e engenharia de software utilizando apenas recursos nativos, com foco nas APIs de I/O e NIO.

- **Manipulação de E/S:** leitura e reconstrução de mensagens binárias utilizando `FileChannel` e `ByteBuffer`, com controle manual de buffer através de `flip`, `compact`, `mark`, `reset` e `remaining`.
- **Controle de acesso a arquivos:** uso de `FileLock` com `tryLock()` para verificar se um arquivo já está sendo utilizado por outra operação.
- **Garantia de persistência:** uso de `force(true)` após a criação do backup para solicitar a persistência das alterações do canal.
- **Invariantes e encapsulamento:** validação das regras essenciais dentro de `Transacao` e proteção da coleção interna de `Lote` com `List.copyOf()`.
- **Tratamento resiliente de exceções:** separação entre exceções de domínio e `IOException`, com isolamento de arquivos que falham durante o processamento.
- **Programação funcional:** uso de `Predicate<Transacao>` e expressões lambda para permitir que regras de validação sejam fornecidas ao `LoteService` sem alterar sua lógica de agrupamento.

## Arquitetura

O projeto utiliza uma organização em camadas simples:

``` text
Controller
    |
    v
Services
    |
    +---- Contracts
    |
    +---- Models
    |
    v
Repository
```

### Controller

`SistemaController` inicializa os componentes e controla o
`WatchService`.

### Services

Contém as regras de processamento:

-   `AmbienteService`: criação do ambiente;
-   `LeitorAgencia`: leitura dos arquivos de agência;
-   `LeitorCaixa`: leitura do protocolo binário;
-   `LoteService`: agrupamento das transações;
-   `ProcessadorService`: coordenação do processamento;
-   `LogService`: auditoria.

### Repository

`ArquivoRepository` concentra as operações relacionadas ao ciclo de vida
físico dos arquivos, como backup, movimentação, quarentena e
reprocessamento.

### Contracts

As interfaces definem os contratos entre as partes do sistema, evitando
que a camada de processamento dependa diretamente das implementações dos
leitores, logger e repositório.

## Uso de Java NIO

O projeto foi construído para explorar diferentes recursos de NIO em
situações que fazem sentido no fluxo:

  Recurso                       Uso
  ----------------------------- ----------------------------------------
  `Path`                        representação dos caminhos do sistema
  `resolve()`                   composição dos diretórios
  `Files.createDirectories()`   criação do ambiente
  `Files.isRegularFile()`       validação de eventos do `WatchService`
  `Files.size()`                obtenção do tamanho dos arquivos
  `Files.move()`                movimentação entre áreas
  `WatchService`                monitoramento das entradas
  `FileChannel`                 leitura binária e operações de backup
  `FileLock`                    verificação de arquivo em uso
  `ByteBuffer`                  reconstrução das mensagens binárias
  `transferTo()`                criação do backup
  `force(true)`                 persistência do canal de backup

## Por que o processamento é síncrono

O projeto mantém o processamento sequencial por decisão de escopo.

O fluxo de cada arquivo possui uma sequência definida de leitura,
validação, preparação, processamento e movimentação. Manter essas
operações no mesmo fluxo facilita o controle do estado dos arquivos, a
previsibilidade do processamento e o tratamento das falhas.

Não foi adotado `AsynchronousFileChannel` porque a complexidade
adicional de um fluxo assíncrono não traz uma vantagem necessária para o
cenário proposto.

Da mesma forma, o projeto não adiciona threads de processamento. O
objetivo é manter o comportamento controlado e utilizar os recursos de
NIO diretamente, sem introduzir tecnologias que não fazem parte do
escopo original.

## Executando

O projeto não utiliza Maven, Gradle ou bibliotecas externas.

O ponto de entrada é:

``` text
src/Main.java
```

Abra o projeto em uma IDE Java e execute a classe `Main`.

Ao iniciar, o sistema cria automaticamente a estrutura:

``` text
banco_aurora/
├── entrada/
│   ├── agencias/
│   ├── caixas/
│   └── retornos/
├── processando/
├── processados/
├── quarentena/
├── reprocessar/
├── logs/
└── backup/
```

Depois que o monitoramento estiver ativo, coloque arquivos `.txt` em:

``` text
banco_aurora/entrada/agencias/
```

ou arquivos `.bin` em:

``` text
banco_aurora/entrada/caixas/
```

O processamento pode ser acompanhado pelo console e pelo arquivo de
auditoria.

## Status

O fluxo principal de ingestão, leitura, validação, auditoria, backup,
processamento e tratamento de falhas está implementado.

O projeto permanece deliberadamente dentro do escopo de Java puro e Java
NIO. Não há persistência em banco de dados, processamento assíncrono,
threads de trabalhadores, reprocessamento automático ou dependências
externas.

O objetivo é demonstrar o uso das APIs e conceitos trabalhados no
projeto, mantendo as decisões de implementação explícitas e o código
compreensível.
