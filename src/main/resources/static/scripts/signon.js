function submitSignon() {
    let emailInput = document.getElementById("signin-email");
    let pwInput = document.getElementById("signin-password");
    if (emailInput.value === "") {
        emailInput.focus();
        return;
    }
    if (pwInput.value === "") {
        pwInput.focus();
        return;
    }
    fetch("/api/auth/authenticate", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: emailInput.value,
            password: pwInput.value
        }),
    }).then((r) => {
        r.json().then(data => {
            if (data["authenticated"] !== undefined && data["authenticated"] === true) {
                swup.navigate("/dashboard");
            }
        });
    });
}


function submitSignup() {
    let emailInput = document.getElementById("signup-email");
    let pwInput = document.getElementById("signup-password");
    let confirmPwInput = document.getElementById("signup-confirm-password");
    if (emailInput.value === "") {
        emailInput.focus();
        return;
    }
    if (pwInput.value === "") {
        pwInput.focus();
        return;
    }
    if (confirmPwInput.value === "") {
        confirmPwInput.focus();
        return;
    }
    fetch("/api/auth/create", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: emailInput.value,
            password: pwInput.value
        }),
    }).then((r) => {
        r.json().then(data => {
            if (data["authenticated"] !== undefined && data["authenticated"] === true) {
                swup.navigate("/dashboard");
            }
        });
    });
}