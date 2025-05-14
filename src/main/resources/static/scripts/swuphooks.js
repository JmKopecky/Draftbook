const swup = new Swup({
    plugins: [new SwupHeadPlugin({
        awaitAssets: true
    }), new SwupScriptsPlugin({
        optin: true
    })]
});


let darkmode = localStorage.getItem("darkmode");
if (darkmode !== null && darkmode === "true") {
    document.getElementsByTagName("html")[0].classList.add("darkmode");
}