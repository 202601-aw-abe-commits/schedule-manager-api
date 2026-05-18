const threadForm = document.getElementById("threadForm");
const threadGameTitleInput = document.getElementById("threadGameTitle");
const threadFirstPostBodyInput = document.getElementById("threadFirstPostBody");
const threadFirstPostScheduleDateInput = document.getElementById("threadFirstPostScheduleDate");
const threadFirstPostStartTimeInput = document.getElementById("threadFirstPostStartTime");
const threadFirstPostDeviceTypeInput = document.getElementById("threadFirstPostDeviceType");
const threadFirstPostRankBandInput = document.getElementById("threadFirstPostRankBand");
const threadFirstPostRecruitmentLimitInput = document.getElementById("threadFirstPostRecruitmentLimit");
const threadDiscordInviteUrlInput = document.getElementById("threadDiscordInviteUrl");
const threadGameTitleSuggestions = document.getElementById("threadGameTitleSuggestions");
const boardCreateMessage = document.getElementById("boardCreateMessage");

const POPULAR_GAME_TITLES = [
    "Apex Legends",
    "VALORANT",
    "Overwatch 2",
    "Monster Hunter Wilds",
    "Fortnite",
    "League of Legends",
    "Minecraft",
    "Call of Duty"
];
const GAME_TITLE_HISTORY_KEY = "board_game_title_history";
const GAME_TITLE_HISTORY_LIMIT = 20;

threadForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    boardCreateMessage.textContent = "";
    const trimmedTitle = normalizeTitle(threadGameTitleInput.value);

    try {
        const created = await fetchJson("/api/board/threads", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ gameTitle: trimmedTitle })
        });
        await fetchJson(`/api/board/threads/${created.id}/posts`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                body: threadFirstPostBodyInput.value,
                scheduleDate: threadFirstPostScheduleDateInput.value || null,
                startTime: threadFirstPostStartTimeInput.value || null,
                deviceType: normalizeTitle(threadFirstPostDeviceTypeInput.value) || "PC",
                rankBand: normalizeTitle(threadFirstPostRankBandInput.value) || null,
                recruitmentLimit: parseIntOrNull(threadFirstPostRecruitmentLimitInput.value),
                discordInviteUrl: normalizeOptionalText(threadDiscordInviteUrlInput.value)
            })
        });
        pushGameTitleHistory(created.gameTitle || trimmedTitle);
        boardCreateMessage.style.color = "#087057";
        boardCreateMessage.textContent = "スレッドと募集投稿を作成しました。募集掲示板へ戻ります。";
        window.location.href = "/board";
    } catch (error) {
        boardCreateMessage.style.color = "#be2f2f";
        boardCreateMessage.textContent = `作成に失敗しました。${error.message}`;
    }
});

function refreshGameTitleSuggestions() {
    const merged = [...POPULAR_GAME_TITLES, ...loadGameTitleHistory()];
    const normalized = Array.from(new Set(merged.map((title) => normalizeTitle(title)).filter((title) => title)));
    threadGameTitleSuggestions.innerHTML = "";
    normalized.slice(0, 50).forEach((title) => {
        const option = document.createElement("option");
        option.value = title;
        threadGameTitleSuggestions.appendChild(option);
    });
}

function loadGameTitleHistory() {
    try {
        const raw = localStorage.getItem(GAME_TITLE_HISTORY_KEY);
        if (!raw) {
            return [];
        }
        const parsed = JSON.parse(raw);
        if (!Array.isArray(parsed)) {
            return [];
        }
        return parsed.map((title) => normalizeTitle(title)).filter((title) => title);
    } catch (error) {
        return [];
    }
}

function pushGameTitleHistory(title) {
    const normalizedTitle = normalizeTitle(title);
    if (!normalizedTitle) {
        return;
    }

    const next = [normalizedTitle, ...loadGameTitleHistory().filter((item) => item !== normalizedTitle)]
        .slice(0, GAME_TITLE_HISTORY_LIMIT);
    try {
        localStorage.setItem(GAME_TITLE_HISTORY_KEY, JSON.stringify(next));
    } catch (error) {
        // Ignore storage errors.
    }
}

function parseIntOrNull(value) {
    if (value == null || value.trim() === "") {
        return null;
    }
    return Number.parseInt(value, 10);
}

function normalizeTitle(value) {
    if (value == null) {
        return "";
    }
    return String(value).trim();
}

function normalizeOptionalText(value) {
    const normalized = normalizeTitle(value);
    return normalized === "" ? null : normalized;
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get("content-type") || "";
    const isJson = contentType.includes("application/json");
    const data = isJson ? await response.json() : null;

    if (!response.ok) {
        const message = data && data.message ? data.message : "通信に失敗しました。";
        throw new Error(message);
    }
    return data;
}

refreshGameTitleSuggestions();
