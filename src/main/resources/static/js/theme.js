(function () {
    const STORAGE_KEY = "themeMode";
    const DARK_CLASS = "dark-mode";
    const BACKGROUND_IMAGE_COUNT = 2;

    function applyTheme(mode) {
        const isDark = mode === "dark";
        document.body.classList.toggle(DARK_CLASS, isDark);
        const button = document.getElementById("themeToggleButton");
        if (button) {
            button.textContent = isDark ? "☀" : "🌙";
            button.setAttribute("aria-pressed", String(isDark));
            button.setAttribute("aria-label", isDark ? "ライトモードへ切替" : "ダークモードへ切替");
            button.setAttribute("title", isDark ? "ライトモードへ切替" : "ダークモードへ切替");
        }
    }

    function currentTheme() {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (saved === "dark" || saved === "light") {
            return saved;
        }
        return "light";
    }

    function buildToggleButton() {
        const button = document.createElement("button");
        button.id = "themeToggleButton";
        button.type = "button";
        button.className = "theme-toggle";
        button.addEventListener("click", () => {
            const next = document.body.classList.contains(DARK_CLASS) ? "light" : "dark";
            localStorage.setItem(STORAGE_KEY, next);
            applyTheme(next);
        });
        document.body.appendChild(button);
    }

    function buildBackgroundImageList() {
        return Array.from({ length: BACKGROUND_IMAGE_COUNT }, (_, index) => {
            return `/img/backgrounds/bg-${index + 1}.png`;
        });
    }

    function applyRandomBackground() {
        const backgroundImages = buildBackgroundImageList();
        if (!backgroundImages.length) {
            return;
        }
        const randomIndex = Math.floor(Math.random() * backgroundImages.length);
        const selected = backgroundImages[randomIndex];
        document.body.style.setProperty("--random-bg-image", `url("${selected}")`);
        document.body.classList.add("random-bg-enabled");
    }

    document.addEventListener("DOMContentLoaded", () => {
        applyRandomBackground();
        buildToggleButton();
        applyTheme(currentTheme());
    });
})();
