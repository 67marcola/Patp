# Verificação adicional em MySQL

PASS em MySQL 8.0.43 temporário, com diretório de dados novo e somente contas fictícias. O banco configurado na aplicação não foi acessado. Essa verificação complementa os testes H2 e não substitui o Verificador independente.

## Suíte de comportamento

Commit do servidor: `99a692e`. Executadas todas as oito classes de comportamento do requisito contra `creral_crud_tests`: 96 testes, zero falhas, erros ou ignorados. A classe `SistemaApplicationTests` tem um critério próprio que exige H2 em memória e permaneceu na execução H2 de 97 testes; nenhum teste de CRUD foi excluído da execução MySQL.

| Classe | Casos | Resultado |
| --- | --- | --- |
| AutenticacaoResponseTests | 2 | PASS |
| UsuarioIdentityTests | 7 | PASS |
| GerenciamentoPersistenceTests | 4 | PASS |
| GerenciamentoApiTests | 48 | PASS |
| EtapaArchiveTests | 6 | PASS |
| ProcessoArchiveTests | 11 | PASS |
| ComentarioArchiveTests | 3 | PASS |
| HistoricoArchiveTests | 15 | PASS |

Comando usado, com propriedades apontando exclusivamente para a instância temporária na porta 33817:

```powershell
mvn.cmd -B test '-Dtest=AutenticacaoResponseTests,UsuarioIdentityTests,GerenciamentoPersistenceTests,GerenciamentoApiTests,EtapaArchiveTests,ProcessoArchiveTests,ComentarioArchiveTests,HistoricoArchiveTests' '-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33817/creral_crud_tests?allowPublicKeyRetrieval=true' '-Dspring.datasource.username=creral_test' '-Dspring.datasource.password=creral_test_local' '-Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver' '-Dspring.jpa.hibernate.ddl-auto=create-drop'
```

A senha no exemplo é fictícia, criada para esta instância temporária. Não copiar `create-drop` para um banco existente. O primeiro lançamento foi corrigido porque o ampersand de uma opção extra da URL foi interpretado pelo wrapper `.cmd` no Windows e interrompeu os argumentos; a segunda execução recebeu todas as propriedades e passou. Não houve alteração de teste para obter o resultado.

Evidência de saída: `%TEMP%/creral-crud-mysql-suite.log`, resumo Maven `Tests run: 96, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`. As assertions permanecem nas classes e linhas citadas em `evidence.md`, inclusive os bloqueios e testes de concorrência.

## Atualização de schema antigo e reinicialização

Executou-se o JAR anterior à mudança de domínio (`f8235b5`) contra outro banco temporário, `creral_legacy_tests`, com `ddl-auto=update`. Foram inseridos sete registros fictícios em seis tabelas: usuário, gerenciamento, etapa, demanda, comentário e dois históricos. O gerenciamento tinha criador nulo e nome de 180 caracteres. Foram capturados todos os valores das colunas antigas antes da atualização.

Após iniciar o JAR novo sobre esse schema, verificou-se:

| Critério | Resultado observado |
| --- | --- |
| Dados legados / GER-20/26: preservar conteúdo e autoria desconhecida | Todas as colunas antigas dos sete registros iguais ao snapshot; criador continua nulo e nome mantém 180 caracteres. |
| GER-26: administrar quadro sem autoria | Funcionário recebe 403; administrador fictício explicitamente selecionado consegue arquivar/restaurar. |
| GER-25: não substituir conta por cadastro | Cadastro recebendo ID 1 recusado com 400; conta existente preservada. |
| GER-17/19: ciclo e idempotência | Arquivar/restaurar retorna 204; repetição bem formada não incrementa versão; filtros correspondem ao estado. |
| GER-21: somente consulta enquanto arquivado | Edição retorna 409; conteúdo anterior permanece intacto. |
| Edge Cases: persistência após restart | Após encerrar o servidor novo e iniciar o JAR `99a692e` sobre o mesmo schema, quadro continua arquivado com versão 3; ativos vazio, arquivados contém o mesmo quadro. |
| GER-20: conteúdo após restart | Nova comparação de todas as colunas antigas, nas seis tabelas, igual ao snapshot inicial. |
| GER-23 / contrato de erro | HTTP real sem sessão e com token inválido retorna 401; decoder UTF-8 estrito e parsing JSON obtêm os dois textos exatos. |

O defeito de charset encontrado nesse exercício foi reproduzido e corrigido por T14; as assertions de HTTP real estão em `AutenticacaoResponseTests.java:38`, `:40`, `:41` e `:43`. A promoção administrativa executada afetou somente a conta fictícia dessa instância.

Artefatos do exercício: pasta `%TEMP%/creral-mysql-crud-be180f80b1f64ead87cc0113c6bd68c1`, incluindo `legacy-db-before.json`, `legacy-migration-result.json` e logs dos JARs antigo/novo. O relatório registra `restartPersistence`, `oldColumnsAfterRestart` e `utf8AuthErrors` como PASS. Esses arquivos contêm apenas dados fictícios e não fazem parte do pacote de produção.

Limite: validação de um schema representativo criado pela versão antiga do código. Não é uma inspeção de schema, backup, collation ou volume da instalação real da Creral. Aplicação no ambiente real continua dependendo da seleção do ambiente e da autorização correspondente.
