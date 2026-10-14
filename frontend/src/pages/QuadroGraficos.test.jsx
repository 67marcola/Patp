import { act, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import Quadro from "./Quadro";
import { buscarConfiguracaoEtapas, arquivarDemanda } from "../services/api";

vi.mock("../services/api", async importar => ({ ...await importar(), buscarConfiguracaoEtapas: vi.fn(), arquivarDemanda: vi.fn() }));
const quadro = { id: 9, nome: "Postes", arquivado: false, podeAdministrar: true, versao: 1 };
const etapas = [{ id: 1, nome: "Trabalho", setor: "Campo", ordem: 1, categoria: "TRABALHO", quantidadeDemandas: 2 }];
const demandas = [1, 2].map(id => ({ id, numeroProcesso: `N${id}`, pessoa: "Cliente", etapaId: 1, status: "Em andamento" }));
const dados = (gerenciamento = quadro) => ({ gerenciamento, etapas, demandas, demandasArquivadas: [] });
const grafico = () => screen.queryByRole("region", { name: "Gráficos do quadro" });

test("GRAF-01/05/06/07: consulta, erro, teclado, leitor arquivado e troca sem dados anteriores", async () => {
    buscarConfiguracaoEtapas.mockReset();
    let rejeitar;
    buscarConfiguracaoEtapas.mockImplementationOnce(() => new Promise((_, reject) => { rejeitar = reject; }));
    const leitor = { ...quadro, podeAdministrar: false, arquivado: true };
    const user = userEvent.setup(), { rerender } = render(<Quadro gerenciamento={leitor} />);
    const botao = screen.getByRole("button", { name: "Gráficos", exact: true });
    expect(botao.disabled).toBe(true); expect(grafico()).toBeNull();
    await act(async () => rejeitar(new Error("Falha na consulta")));
    expect(screen.getByRole("alert").textContent).toBe("Falha na consulta");
    expect(botao.disabled).toBe(true); expect(grafico()).toBeNull();
    buscarConfiguracaoEtapas.mockResolvedValueOnce(dados(leitor));
    await user.click(screen.getByRole("button", { name: "Tentar novamente" }));
    await waitFor(() => expect(botao.disabled).toBe(false));
    expect(botao.getAttribute("aria-expanded")).toBe("false");
    botao.focus(); await user.keyboard("{Enter}");
    expect(botao.getAttribute("aria-expanded")).toBe("true");
    expect(botao.getAttribute("aria-controls")).toBe(grafico().id); expect(document.activeElement).toBe(botao);
    expect(within(grafico()).getByText("Total não arquivadas: 2")).toBeTruthy();
    await user.keyboard("{Enter}"); expect(grafico()).toBeNull();
    await user.click(botao); expect(grafico()).toBeTruthy();
    expect(buscarConfiguracaoEtapas.mock.calls.length).toBe(2);
    let resolver;
    buscarConfiguracaoEtapas.mockImplementationOnce(() => new Promise(resolve => { resolver = resolve; }));
    const outro = { ...leitor, id: 10, nome: "Outro" };
    rerender(<Quadro gerenciamento={outro} />);
    expect(grafico()).toBeNull(); expect(botao.disabled).toBe(true);
    await act(async () => resolver({ gerenciamento: outro, etapas: [], demandas: [], demandasArquivadas: [] }));
    expect(grafico()).toBeNull(); expect(botao.getAttribute("aria-expanded")).toBe("false");
    await user.click(botao); expect(within(grafico()).getByText("Total não arquivadas: 0")).toBeTruthy();
});

test("GRAF-04/05/06: ação atualiza totais; conflito oculta os números até nova consulta", async () => {
    buscarConfiguracaoEtapas.mockReset(); arquivarDemanda.mockReset();
    buscarConfiguracaoEtapas.mockResolvedValueOnce(dados());
    const user = userEvent.setup(); render(<Quadro gerenciamento={quadro} />);
    await waitFor(() => expect(screen.getByRole("button", { name: "Gráficos" }).disabled).toBe(false));
    await user.click(screen.getByRole("button", { name: "Gráficos" }));
    const salvo = { ...dados(), gerenciamento: { ...quadro, versao: 2 }, demandas: [demandas[1]], demandasArquivadas: [demandas[0]] };
    arquivarDemanda.mockResolvedValueOnce(salvo);
    await user.click(screen.getByRole("button", { name: "Arquivar demanda N1" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar demanda", exact: true }));
    expect(within(grafico()).getByText("Total não arquivadas: 1")).toBeTruthy();
    expect(within(grafico()).getByText("Arquivadas: 1")).toBeTruthy();
    arquivarDemanda.mockRejectedValueOnce(new Error("Versão desatualizada"));
    await user.click(screen.getByRole("button", { name: "Arquivar demanda N2" }));
    await user.click(within(screen.getByRole("dialog")).getByRole("button", { name: "Arquivar demanda", exact: true }));
    expect(grafico()).toBeNull(); expect(screen.getByRole("button", { name: "Gráficos" }).disabled).toBe(true);
    buscarConfiguracaoEtapas.mockResolvedValueOnce(salvo);
    await user.click(screen.getByRole("button", { name: "Atualizar quadro" }));
    expect(within(grafico()).getByText("Total não arquivadas: 1")).toBeTruthy();
});

test("GRAF-09: alternar gráficos preserva formulário e rascunho", async () => {
    buscarConfiguracaoEtapas.mockReset(); buscarConfiguracaoEtapas.mockResolvedValueOnce(dados());
    const user = userEvent.setup(); render(<Quadro gerenciamento={quadro} />);
    await waitFor(() => expect(screen.getByRole("button", { name: "+ Criar processo" }).disabled).toBe(false));
    await user.click(screen.getByRole("button", { name: "+ Criar processo" }));
    await user.type(screen.getByRole("textbox", { name: "Cliente/solicitante" }), "Rascunho preservado");
    await user.click(screen.getByRole("button", { name: "Gráficos" }));
    expect(grafico()).toBeTruthy(); expect(screen.getByRole("textbox", { name: "Cliente/solicitante" }).value).toBe("Rascunho preservado");
    await user.click(screen.getByRole("button", { name: "Gráficos" }));
    expect(screen.getByRole("textbox", { name: "Cliente/solicitante" }).value).toBe("Rascunho preservado");
    expect(buscarConfiguracaoEtapas.mock.calls.length).toBe(1);
});
