const incomingRequestList = document.getElementById("incomingRequestList");
const outgoingRequestList = document.getElementById("outgoingRequestList");
const friendMessage = document.getElementById("friendMessage");

async function loadRequests() {
    try {
        const data = await fetchJson("/api/friends");
        renderIncomingRequests(data.incomingRequests || []);
        renderOutgoingRequests(data.outgoingRequests || []);
    } catch (error) {
        friendMessage.style.color = "#be2f2f";
        friendMessage.textContent = error.message;
    }
}

function renderIncomingRequests(incomingRequests) {
    incomingRequestList.innerHTML = "";
    if (!Array.isArray(incomingRequests) || incomingRequests.length === 0) {
        incomingRequestList.innerHTML = "<li>受信した申請はありません。</li>";
        return;
    }

    incomingRequests.forEach((request) => {
        const li = document.createElement("li");
        li.textContent = `${request.requesterDisplayName} (@${request.requesterUsername})`;

        const acceptButton = document.createElement("button");
        acceptButton.type = "button";
        acceptButton.textContent = "承認";
        acceptButton.addEventListener("click", async () => {
            try {
                await fetchJson(`/api/friends/requests/${request.id}/accept`, { method: "POST" });
                friendMessage.style.color = "#087057";
                friendMessage.textContent = "フレンド申請を承認しました。";
                await loadRequests();
            } catch (error) {
                friendMessage.style.color = "#be2f2f";
                friendMessage.textContent = error.message;
            }
        });

        li.appendChild(acceptButton);
        incomingRequestList.appendChild(li);
    });
}

function renderOutgoingRequests(outgoingRequests) {
    outgoingRequestList.innerHTML = "";
    if (!Array.isArray(outgoingRequests) || outgoingRequests.length === 0) {
        outgoingRequestList.innerHTML = "<li>送信中の申請はありません。</li>";
        return;
    }
    outgoingRequests.forEach((request) => {
        const li = document.createElement("li");
        li.textContent = `${request.requesterDisplayName} (@${request.requesterUsername})`;
        outgoingRequestList.appendChild(li);
    });
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

loadRequests();
