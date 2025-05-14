function toggleDarkMode() {
    let body = document.getElementsByTagName("html")[0];
    if (body.classList.contains("darkmode")) {
        body.classList.remove("darkmode");
        localStorage.setItem("darkmode", "false");
    } else {
        body.classList.add("darkmode")
        localStorage.setItem("darkmode", "true");
    }
}