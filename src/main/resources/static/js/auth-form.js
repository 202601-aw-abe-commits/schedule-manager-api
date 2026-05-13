function wireEnterSubmit(formId, submitButtonId) {
    const form = document.getElementById(formId);
    const submitButton = document.getElementById(submitButtonId);
    if (!form || !submitButton) {
        return;
    }

    const fields = form.querySelectorAll("input, textarea, select");
    fields.forEach((field) => {
        field.addEventListener("keydown", (event) => {
            if (event.key !== "Enter" || event.shiftKey || event.isComposing) {
                return;
            }
            event.preventDefault();
            form.requestSubmit(submitButton);
        });
    });
}

wireEnterSubmit("loginForm", "loginSubmitButton");
wireEnterSubmit("registerForm", "registerSubmitButton");
