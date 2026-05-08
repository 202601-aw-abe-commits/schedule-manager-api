const dmSearchForm = document.getElementById("dmSearchForm");
const dmSearchInput = document.getElementById("dmSearchInput");
const dmConversationCards = document.getElementById("dmConversationCards");
const dmMessage = document.getElementById("dmMessage");

let conversations = [];

if (dmSearchInput) {
    dmSearchInput.addEventListener("input", () => {
        dmMessage.textContent = "";
        renderConversationCards(filterConversations(dmSearchInput.value));
    });
}

if (dmSearchForm) {
    dmSearchForm.addEventListener("submit", (event) => {
        event.preventDefault();
        dmMessage.textContent = "";
        const keyword = String(dmSearchInput ? dmSearchInput.value : "").trim();
        const filtered = filterConversations(keyword);
        renderConversationCards(filtered);
        if (!keyword) {
            return;
        }
        const exact = filtered.find((row) => String(row.partnerUsername || "").toLowerCase() === keyword.toLowerCase());
        if (exact && exact.id) {
            window.location.href = `/dm/conversations/${exact.id}`;
            return;
        }
        dmMessage.style.color = "#4b5f88";
        dmMessage.textContent = `検索結果: ${filtered.length}件`;
    });
}

async function loadConversations() {
    try {
        conversations = await fetchJson("/dm/conversations");
        renderConversationCards(filterConversations(dmSearchInput ? dmSearchInput.value : ""));
    } catch (error) {
        dmMessage.style.color = "#be2f2f";
        dmMessage.textContent = error.message;
    }
}

function filterConversations(keyword) {
    const text = String(keyword || "").trim().toLowerCase();
    if (!text) {
        return conversations;
    }
    return conversations.filter((row) => {
        const name = `${row.partnerDisplayName || ""} ${row.partnerUsername || ""}`.toLowerCase();
        const last = String(row.lastMessageBody || "").toLowerCase();
        return name.includes(text) || last.includes(text);
    });
}

function renderConversationCards(rows) {
    dmConversationCards.innerHTML = "";
    if (!Array.isArray(rows) || rows.length === 0) {
        dmConversationCards.innerHTML = "<li>該当する会話はありません。</li>";
        return;
    }

    rows.forEach((row) => {
        const li = document.createElement("li");
        li.className = "dm-conversation-card";

        const link = document.createElement("a");
        link.href = `/dm/conversations/${row.id}`;
        link.className = "dm-conversation-link";

        const avatar = document.createElement("div");
        avatar.className = "dm-avatar";
        avatar.textContent = extractInitial(row.partnerDisplayName, row.partnerUsername);

        const body = document.createElement("div");
        body.className = "dm-conversation-body";

        const title = document.createElement("div");
        title.className = "dm-conversation-title";
        title.textContent = `${row.partnerDisplayName || row.partnerUsername || "unknown"} (@${row.partnerUsername || "-"})`;

        const preview = document.createElement("div");
        preview.className = "dm-conversation-preview";
        preview.textContent = row.lastMessageBody || "まだメッセージはありません。";

        body.appendChild(title);
        body.appendChild(preview);
        link.appendChild(avatar);
        link.appendChild(body);
        li.appendChild(link);
        dmConversationCards.appendChild(li);
    });
}

function extractInitial(displayName, username) {
    const source = String(displayName || username || "?").trim();
    return source ? source.slice(0, 1) : "?";
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

loadConversations();
