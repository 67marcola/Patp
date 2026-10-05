# Etapas: validação adicional no MySQL

**Resultado: PASS.** Os 237 testes de comportamento passaram no MySQL 8.0.43, com zero falhas, erros ou testes ignorados. Execução em 2026-10-05, código do servidor no commit `c2e3cce`. Tempo do Maven: 20,601 segundos.

## Isolamento

A instância foi iniciada com `--no-defaults`, endereço `127.0.0.1`, porta `33817` e dados em `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/data`. Processo e porta foram conferidos contra essa pasta antes dos testes e do encerramento. O usuário `creral_test` e a senha `creral_test_local` são exclusivamente fictícios.

O schema descartável `creral_crud_tests` usou `create-drop`. A aplicação normal não foi iniciada; não foi usada nem alterada sua conexão MySQL configurada. O schema fictício separado `creral_legacy_tests`, da validação anterior, conservou as mesmas contagens antes/depois: um gerenciamento, uma etapa, uma demanda, um comentário, dois históricos e um usuário. O gerenciamento continuou arquivado com versão 3. Essa comparação comprova as contagens e o estado consultados; não representa uma nova execução da migração histórica documentada no primeiro CRUD.

O log confirma o banco realmente usado pelos contextos Spring: `etapas-maven-mysql.log:70` e `:1821` mostram a URL acima; `:71`/`:1822` mostram `MySQL Connector/J`; `:73`/`:1824` mostram a versão `8.0.43`.

Depois dos testes, `mysqladmin --no-defaults --protocol=TCP --host=127.0.0.1 --port=33817 --user=root shutdown` encerrou a instância. A porta ficou livre e o PID real `23944` deixou de existir. Os arquivos temporários de evidência e dados fictícios foram preservados.

## Comando executado

Em `sistema`, com `JAVA_HOME=C:/Program Files/Java/jdk-25.0.2`:

```powershell
mvn.cmd -B test `
  '-Dtest=AutenticacaoResponseTests,UsuarioIdentityTests,GerenciamentoPersistenceTests,GerenciamentoApiTests,EtapaArchiveTests,ProcessoArchiveTests,ComentarioArchiveTests,HistoricoArchiveTests,EtapaApiTests,EtapaDemandConcurrencyTests' `
  '-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33817/creral_crud_tests?allowPublicKeyRetrieval=true' `
  '-Dspring.datasource.username=creral_test' `
  '-Dspring.datasource.password=creral_test_local' `
  '-Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver' `
  '-Dspring.jpa.hibernate.ddl-auto=create-drop'
```

`SistemaApplicationTests` permanece no gate H2: seu critério específico verifica que a URL de teste usa H2. Não foi desabilitado nem alterado para este comando. O gate completo H2 executou 238 testes, incluindo esse caso, conforme `evidence.md`.

## Resultados por classe

Contagens extraídas dos XMLs Surefire das dez classes selecionadas, sem incluir relatórios anteriores de outras classes. A captura está em `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/etapas-mysql-surefire-summary.json`. Os XMLs da pasta target são reutilizados pelas execuções seguintes; a captura e o log preservam este resultado MySQL.

| Classe | Passaram | Falhas/erros/ignorados |
| --- | --- | --- |
| AutenticacaoResponseTests | 2 | 0/0/0 |
| UsuarioIdentityTests | 7 | 0/0/0 |
| GerenciamentoPersistenceTests | 4 | 0/0/0 |
| GerenciamentoApiTests | 48 | 0/0/0 |
| EtapaArchiveTests | 6 | 0/0/0 |
| ProcessoArchiveTests | 11 | 0/0/0 |
| ComentarioArchiveTests | 3 | 0/0/0 |
| HistoricoArchiveTests | 15 | 0/0/0 |
| EtapaApiTests | 134 | 0/0/0 |
| EtapaDemandConcurrencyTests | 7 | 0/0/0 |
| Total | 237 | 0/0/0 |

Os mesmos testes derivados de ETA-01–21 verificaram snapshots, contagens, permissões, campos/limites, posições, IDs, versões, preservação, rollback e concorrência neste banco. As corridas controladas cobrem remoção disputando com criação/movimentação de demanda, duas configurações usando a mesma versão e consulta esperando uma configuração confirmar. As relações persistidas e os históricos são conferidos após as operações. A matriz de assertions e os valores exigidos pela spec estão em `evidence.md`.

Log da execução: `C:/Users/Marco/AppData/Local/Temp/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1/etapas-maven-mysql.log`. O Maven terminou com código 0 e `BUILD SUCCESS`. Esta evidência adicional do servidor não substitui T2–T5, a verificação independente nem o teste humano da interface.
