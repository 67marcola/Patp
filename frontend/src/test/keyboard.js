import { expect } from "vitest";

export async function ativarPorTeclado(user, controle) {
    const focaveis = [...document.querySelectorAll("button, input, select, textarea, a[href], [tabindex]")].filter(elemento => {
        if (elemento.tabIndex < 0 || elemento.matches(":disabled")) return false;
        for (let atual = elemento; atual; atual = atual.parentElement) {
            const estilo = getComputedStyle(atual);
            if (atual.hidden || estilo.display === "none" || estilo.visibility === "hidden") return false;
        }
        return true;
    });
    for (let tentativa = 0; tentativa < focaveis.length + 1 && document.activeElement !== controle; tentativa += 1) {
        await user.tab();
    }
    expect(document.activeElement).toBe(controle);
    await user.keyboard("{Enter}");
}
