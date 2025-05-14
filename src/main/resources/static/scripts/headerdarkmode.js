function toggleDarkMode() {
    let body = document.getElementsByTagName("html")[0];
    if (body.classList.contains("darkmode")) {
        body.classList.remove("darkmode");
    } else {
        body.classList.add("darkmode")
    }
}