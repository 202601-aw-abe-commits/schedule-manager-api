(function () {
    function normalizeTitle(value) {
        if (value == null) {
            return "";
        }
        return String(value).trim();
    }

    function getBadge(title) {
        const t = normalizeTitle(title).toLowerCase();
        if (t.includes("apex")) return "🎯";
        if (t.includes("valorant")) return "🔫";
        if (t.includes("overwatch")) return "🛡️";
        if (t.includes("monster hunter") || t.includes("モンハン")) return "🐉";
        if (t.includes("fortnite")) return "🏗️";
        if (t.includes("league of legends") || t.includes("lol")) return "⚔️";
        if (t.includes("minecraft")) return "⛏️";
        if (t.includes("call of duty") || t.includes("cod")) return "🎖️";
        if (t.includes("splatoon") || t.includes("スプラ")) return "🦑";
        if (t.includes("マリオ") || t.includes("mario")) return "🍄";
        if (t.includes("ポケモン") || t.includes("pokemon")) return "⚡";
        return "🎮";
    }

    function withBadge(title) {
        const normalized = normalizeTitle(title);
        if (!normalized) {
            return normalized;
        }
        return `${getBadge(normalized)} ${normalized}`;
    }

    window.GameBadge = {
        get: getBadge,
        withBadge,
        normalizeTitle
    };
})();
