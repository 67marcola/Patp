# Cadastro e sessão no MySQL temporário

PASS: os nove casos novos de `CadastroSessaoTests` passaram em MySQL 8.0.43 sobre o controlador do commit `1a70758`, com zero falhas, erros ou skips. Este teste complementa o gate completo H2; não substitui a validação independente da feature.

## Ambiente e execução

- Instância reutilizada apenas do diretório fictício `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/data`.
- Executável MySQL Server 8.0, `--no-defaults`, bind `127.0.0.1`, porta `33817`, mysqlx desativado. Nenhuma leitura do arquivo de configuração ou acesso ao MySQL da aplicação.
- Banco de teste `creral_crud_tests`, usuário de teste fictício, driver MySQL explícito e `ddl-auto=create-drop`. Schema legado temporário não foi alvo desta execução. Nenhuma migração ou teste de schema novo é reivindicado.
- JDK `C:/Program Files/Java/jdk-25.0.2`. Comando Maven `mvn.cmd -B test -Dtest=CadastroSessaoTests` com datasource MySQL de teste passado por propriedades. Exit 0 e BUILD SUCCESS em 05/10/2026, 20:33:03, horário de São Paulo.
- Surefire XML conferido: `tests=9`, `failures=0`, `errors=0`, `skipped=0`. Tempo Maven 9,293 s.

Os casos usam HTTP real em porta aleatória e verificam cadastro inicial/seguinte, papel funcionário, BCrypt, DTO sem senha/hash, token ligado ao ID salvo e utilizável, ID recebido existente/escolhido, JSON malformado, email duplicado, falha simulada de persistência e login manual funcionário/admin com setor nulo legado. Assertions e mapeamentos AUT-01–05 estão em `evidence.md` e `sistema/src/test/java/com/patp/sistema/CadastroSessaoTests.java`.

## Evidência e encerramento

Log integral preservado em `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/autocadastro-maven-mysql.log`. Resumo de XML preservado no mesmo diretório em `autocadastro-mysql-surefire-summary.json`.

Antes do teste e do encerramento, o processo dono da porta foi conferido via CIM: `mysqld.exe`, PID 15660, linha de comando com `--no-defaults` e o datadir TEMP exato. Encerramento por `mysqladmin` restrito a localhost:33817, exit 0; porta confirmada livre. Diretório temporário preservado. Serviços do usuário em 8081/5173 não foram alterados.
