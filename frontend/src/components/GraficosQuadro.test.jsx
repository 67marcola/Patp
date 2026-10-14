import { render, screen, within } from "@testing-library/react";
import { expect, test } from "vitest";
import GraficosQuadro from "./GraficosQuadro";

const etapas = [{ id: 1, nome: "Trabalho" }, { id: 2, nome: "Trabalho" }, { id: 3, nome: "Concluídos" }, { id: 4, nome: "Cancelados" }];
const demanda = (id, status, etapaId) => ({ id, status, etapaId });
function linhas(nome) {
    return within(screen.getByRole("group", { name: nome })).getAllByRole("listitem").map(e => e.textContent);
}

test("GRAF-02/03/04/09: totais exatos, nomes iguais por ID, legados e arquivadas separadas", () => {
    render(<GraficosQuadro etapas={etapas} demandas={[
        demanda(1, "Em andamento", 1), demanda(2, "Em andamento", 1), demanda(3, "Concluido", 3),
        demanda(4, "Cancelado", 4), demanda(5, null, 2), demanda(6, "Antigo", 99)
    ]} demandasArquivadas={[demanda(7, "Em andamento", 1)]} />);
    expect(linhas("Demandas por situação")).toEqual(["Em andamento2", "Concluídas1", "Canceladas1", "Não informado2"]);
    expect(linhas("Demandas por etapa")).toEqual(["Trabalho2", "Trabalho1", "Concluídos1", "Cancelados1", "Etapa não identificada1"]);
    expect(screen.getByText("Total não arquivadas: 6")).toBeTruthy();
    expect(screen.getByText("Arquivadas: 1")).toBeTruthy();
    const barras = screen.getByRole("region", { name: "Gráficos do quadro" }).querySelectorAll(".grafico-barra");
    expect([...barras].every(b => b.getAttribute("aria-hidden") === "true")).toBe(true);
    expect([...barras].map(b => b.style.width)).toEqual(["100%", "50%", "50%", "100%", "100%", "50%", "50%", "50%", "50%"]);
});

test("GRAF-08: vazio mantém estados e etapas com zero, sem barra inválida", () => {
    render(<GraficosQuadro etapas={etapas} demandas={[]} demandasArquivadas={[]} />);
    expect(screen.getByText("Nenhuma demanda não arquivada neste quadro.")).toBeTruthy();
    expect(linhas("Demandas por situação")).toEqual(["Em andamento0", "Concluídas0", "Canceladas0"]);
    expect(linhas("Demandas por etapa")).toEqual(["Trabalho0", "Trabalho0", "Concluídos0", "Cancelados0"]);
    expect([...document.querySelectorAll(".grafico-barra")].every(b => b.style.width === "0%")).toBe(true);
});

test("GRAF-03/05/09: ausência de etapas, texto literal e nova configuração", () => {
    const { rerender } = render(<GraficosQuadro etapas={[]} demandas={[demanda(1, "Em andamento", null)]} demandasArquivadas={[]} />);
    expect(linhas("Demandas por etapa")).toEqual(["Etapa não identificada1"]);
    rerender(<GraficosQuadro etapas={[{ id: 2, nome: "<img src=x onerror=alert(1)>" }]} demandas={[demanda(1, "Concluido", 2)]} demandasArquivadas={[demanda(2, "Cancelado", 2)]} />);
    expect(linhas("Demandas por situação")).toEqual(["Em andamento0", "Concluídas1", "Canceladas0"]);
    expect(linhas("Demandas por etapa")).toEqual(["<img src=x onerror=alert(1)>1"]);
    expect(screen.getByText("Total não arquivadas: 1")).toBeTruthy();
    expect(screen.getByText("Arquivadas: 1")).toBeTruthy();
    expect(document.querySelector("img")).toBeNull();
});
