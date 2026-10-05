import { defineConfig } from "@playwright/test";

export default defineConfig({
    testDir: "./e2e",
    fullyParallel: false,
    workers: 1,
    retries: 0,
    timeout: 60000,
    webServer: {
        command: "node scripts/isolated-system.mjs",
        url: "http://localhost:4173",
        reuseExistingServer: false,
        timeout: 180000,
        gracefulShutdown: { signal: "SIGINT", timeout: 10000 }
    },
    use: {
        baseURL: "http://localhost:4173",
        channel: "msedge",
        headless: true,
        trace: "retain-on-failure"
    },
    reporter: "list"
});
