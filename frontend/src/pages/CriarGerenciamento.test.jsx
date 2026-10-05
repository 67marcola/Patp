import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { expect, test, vi } from "vitest";
import CriarGerenciamento from "./CriarGerenciamento";

test("GER-31: cancelar o formulário retorna sem enviar mutação", async () => {
    const fetchSpy = vi.fn();
    vi.stubGlobal("fetch", fetchSpy);
    function Fluxo() {
        const [formulario, setFormulario] = useState(true);
        return formulario
            ? <CriarGerenciamento voltar={() => setFormulario(false)} atualizar={() => {}} />
            : <h1>Quadro de instalações</h1>;
    }
    const user = userEvent.setup();
    render(<Fluxo />);
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.getByRole("heading", { name: "Quadro de instalações" }).textContent)
        .toBe("Quadro de instalações");
    expect(fetchSpy).not.toHaveBeenCalled();
});
