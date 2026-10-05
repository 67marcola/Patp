# Entrada após cadastro: desenho

## Fluxo

`Cadastro -> POST /usuarios/cadastro -> UsuarioService.cadastrar -> usuário salvo -> SessaoService.criarSessao -> LoginResponse -> cliente valida resposta -> App.entrar -> localStorage -> Gerenciamentos`.

O login manual usa o mesmo DTO e a mesma entrada de App. Não fazer uma segunda chamada de login depois do cadastro. Criar sessão somente após o serviço devolver o usuário persistido.

## Componentes

- `UsuarioController`: devolver LoginResponse no cadastro; usar helper privado comum de resposta somente se reduzir a duplicação com login. Preservar entidade de entrada e serviço, pois ID enviado deve continuar rejeitado.
- `api.js`: helper pequeno comum aos dois wrappers de autenticação verifica os cinco campos exigidos para a entrada. Reusar ApiError/parser; não adicionar validação genérica de outras rotas nem dependências.
- `App.entrar`: construir projeção segura do usuário, capturar cache anterior, gravar ambas as chaves antes de abrir a tela. Em falha, tentar restaurar ambas e lançar a mensagem definida. Não redesenhar boot/logout.
- `Login`: passar resposta ao callback compartilhado, remover apenas sua gravação duplicada de sessão. Manter credenciais e envio existentes; erro do callback aparece no formulário.
- `Cadastro`: receber callback de entrada, ref de envio para barrar duplicação imediata, campos/ações disabled enquanto aguarda. Associar labels, alert acessível. Mapear resposta não confirmada para texto do cadastro; preservar rascunho em toda falha.

## Verificação

Integração Spring/H2 do DTO e identidade persistida; cliente Vitest dos payloads e respostas; RTL do estado/cache e formulário; novo E2E isolado sem modificar os anteriores. Usar JDK real e helper existente. Não abrir MySQL configurado. Nenhuma mudança de esquema.

Verificador novo após T5 examina AUT-01–14, executa gates e injeta falhas em cópias TEMP. Exemplos de superfícies críticas: sessão associada ao ID errado, papel indevido, resposta sem token aceita, cache com senha, cadastro voltando ao login e envios duplicados. Mutantes efetivos serão escolhidos pelo Verificador.
