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
    //todo implement

    //note temporary send to account page
    swup.navigate("/account");
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

    //todo implement

    //note temporary send to account page
    swup.navigate("/account");
}