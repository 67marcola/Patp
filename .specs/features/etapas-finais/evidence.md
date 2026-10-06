# Evidência de execução backend

## T1: categoria persistida e DTO

Pré-plano: categoria nullable STRING; getter null retorna TRABALHO, sem inferir nome nem escrever. Arquivos: CategoriaEtapa.java, Etapa.java, EtapaResponse.java, EtapaService.java, EtapaCategoriaTests.java e spec/tasks/evidence. Sucesso: persistência das três categorias e snapshot HTTP com campos/contagem reais; SQL null permanece null após GET. Sem mudança de fixtures globais.

Gate: `mvn.cmd -B verify`, JDK 25, classpath H2: **249 testes, 0 falhas/erros/skips**. Log: `C:/Users/Marco/AppData/Local/Temp/etapas-finais-backend-20261006/T1-verify.log`.

| AC/done when | Evidência + assertion | Resultado | Cobertura |
| --- | --- | --- | --- |
| Enum STRING persistido/devolvido | `sistema/src/test/java/com/patp/sistema/EtapaCategoriaTests.java:28` `.isEqualTo(categoria.name())`; `:30` `.getCategoria()).isEqualTo(categoria)` | TRABALHO/CONCLUIDA/CANCELADA | Sim |
| FIN-06 legado null/homônimo sem escrita | mesmo arquivo `:48` `jsonPath("$.etapas[0].categoria").value("TRABALHO")`; `:50` `assertThat(conteudoPersistido()).isEqualTo(antes)`; `:51` `.isNull()` | trabalho, SQL null intacto | Sim |
| FIN-07 identidade/campos/contagem | mesmo arquivo `:44–49` `value(etapa.getId())`, `value("Concluídos")`, `value("Engenharia")`, `value(7)`, `value("TRABALHO")`, `value(1)` | campos reais | Sim |

| Assertion (arquivo/linha acima) | Requisito | Manter |
| --- | --- | --- |
| :28/:30 persistência enum | T1 done when | Sim |
| :43–49 snapshot inteiro | FIN-06/07 | Sim |
| :50/:51 snapshot igual/SQL null | FIN-06 | Sim |

Adequação PASS: expectativas derivadas da spec, sem assertions rasas, campos individuais verificados, dois testes novos necessários. Padrões SpringBootTest/MockMvc do repositório seguidos. Nenhum teste alterado/excluído/skip, nenhuma SPEC_DEVIATION.

