# Operação local do sistema Creral

Os testes Maven usam H2 em memória pelo classpath de teste. Execute `mvn.cmd -B verify` em `sistema/`. A aplicação normal continua usando MySQL. Não execute o perfil normal para verificar testes.

## Selecionar administrador

O cadastro público sempre cria funcionário, rejeita ID enviado e não permite escolher papel. O primeiro cadastro não recebe privilégios especiais.

Depois que uma conta escolhida pelo responsável já estiver cadastrada, um operador autorizado pode conferir seu ID e e-mail no banco correto e atualizar somente essa conta. O exemplo abaixo é documentação; não foi executado no banco configurado:

```sql
START TRANSACTION;
SELECT id, email, papel FROM usuarios WHERE id = :id_escolhido AND email = :email_conferido;
UPDATE usuarios SET papel = 'ADMINISTRADOR'
WHERE id = :id_escolhido AND email = :email_conferido;
-- Conferir que exatamente uma conta foi atualizada antes de confirmar.
COMMIT;
```

Substitua os parâmetros por valores conferidos. Não use IDs futuros, e-mails ainda não cadastrados nem atualização sem `WHERE`. Para retirar o papel, a mesma seleção explícita usa `papel = 'FUNCIONARIO'`. A sessão resolve o usuário persistido em cada requisição; alterações confiáveis de papel são reconhecidas sem depender do objeto salvo no login.

Contas antigas com papel nulo são tratadas como funcionários. Não há promoção automática de contas reais.

## Estrutura de gerenciamentos legados

Antes de aplicar uma atualização no ambiente escolhido, conferir backup, schema e nomes das colunas no Workbench. Os exemplos abaixo correspondem ao modelo atual e não foram executados no banco configurado. Não repetir `ADD COLUMN` se a coluna já existir.

```sql
ALTER TABLE usuarios ADD COLUMN papel VARCHAR(20) DEFAULT 'FUNCIONARIO';
ALTER TABLE gerenciamentos ADD COLUMN arquivado BOOLEAN DEFAULT FALSE;
ALTER TABLE gerenciamentos ADD COLUMN versao BIGINT DEFAULT 0;
UPDATE usuarios SET papel = 'FUNCIONARIO' WHERE papel IS NULL;
UPDATE gerenciamentos SET versao = 0 WHERE versao IS NULL;
```

Criador nulo permanece nulo; somente administrador administra esses quadros. O marcador de arquivamento nulo é lido como ativo. As colunas nome/descrição continuam com 255 caracteres, evitando reduzir ou truncar nomes antigos. A API limita novos nomes a 120 caracteres, sem alterar os valores antigos durante consultas.

H2 valida o contrato em memória. MySQL isolado deve verificar atualização de schema e diferenças de collation/locking; nunca usar o banco de dados configurado como instância de teste por suposição.
