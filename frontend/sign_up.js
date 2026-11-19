const statusMessage = document.querySelector("#status");
const registerPanel = document.querySelector("#register-panel");
const loginPanel = document.querySelector("#login-panel");
const accountPanel = document.querySelector("#account-panel");

if (window.location.pathname === "/login") {
    registerPanel.hidden = true;
    loginPanel.hidden = false;
}

function showStatus(message) {
    statusMessage.textContent = message;
}

async function request(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options.headers
        }
    });
    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "The request failed.");
    }

    return result;
}

function showAccount(user) {
    registerPanel.hidden = true;
    loginPanel.hidden = true;
    accountPanel.hidden = false;
    document.querySelector("#account-info").textContent =
        `Signed in as user ${user.id}.`;
}

document.querySelector("#show-login").addEventListener("click", () => {
    registerPanel.hidden = true;
    loginPanel.hidden = false;
    showStatus("");
});

document.querySelector("#show-register").addEventListener("click", () => {
    loginPanel.hidden = true;
    registerPanel.hidden = false;
    showStatus("");
});

document.querySelector("#register-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
        await request("/api/register", {
            method: "POST",
            body: JSON.stringify({
                email: form.get("email"),
                password: form.get("password")
            })
        });
        formElement.reset();
        registerPanel.hidden = true;
        loginPanel.hidden = false;
        showStatus("Account created. You can now log in.");
    } catch (error) {
        showStatus(error.message);
    }
});

document.querySelector("#login-form").addEventListener("submit", async event => {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);

    try {
        await request("/api/login", {
            method: "POST",
            body: JSON.stringify({
                email: form.get("email"),
                password: form.get("password")
            })
        });
        formElement.reset();
        showAccount(await request("/api/me"));
        showStatus("Login successful.");
    } catch (error) {
        showStatus(error.message);
    }
});

document.querySelector("#logout-button").addEventListener("click", async () => {
    try {
        await request("/api/logout", {method: "POST"});
        accountPanel.hidden = true;
        loginPanel.hidden = false;
        showStatus("You have logged out.");
    } catch (error) {
        showStatus(error.message);
    }
});

request("/api/me")
    .then(showAccount)
    .catch(error => {
        if (error.message !== "Not logged in") {
            showStatus(error.message);
        }
    });
