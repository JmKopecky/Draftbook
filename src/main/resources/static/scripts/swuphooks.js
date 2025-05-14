const swup = new Swup({
    plugins: [new SwupHeadPlugin({
        awaitAssets: true
    }), new SwupScriptsPlugin({
        optin: true
    })]
});
