import { expect } from "vitest";

export async function ativarPorTeclado(user, controle) {
    for (let tentativa = 0; tentativa < 30 && document.activeElement !== controle; tentativa += 1) {
        await user.tab();
    }
    expect(document.activeElement).toBe(controle);
    await user.keyboard("{Enter}");
}
