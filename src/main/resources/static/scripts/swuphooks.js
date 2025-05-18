const swup = new Swup({
    plugins: [new SwupHeadPlugin({
        awaitAssets: true
    }), new SwupScriptsPlugin({
        optin: true
    })]
});


document.addEventListener("DOMContentLoaded", () => {
    createTextEditor();
    swup.hooks.on("visit:start", (visit) => {
        let url = visit.to.url;
        let header = document.getElementsByTagName("header")[0];
        if (url.includes("/chapter/") || url.includes("/note/")) {
            header.classList.add("editor");
        } else {
            header.classList.remove("editor");
        }
    });
    swup.hooks.on("visit:end", (visit) => {
        let url = visit.to.url;
        if (url.includes("/chapter/") || url.includes("/note/")) {
            createTextEditor();
        }
    })
});


let darkmode = localStorage.getItem("darkmode");
if (darkmode !== null && darkmode === "true") {
    document.getElementsByTagName("html")[0].classList.add("darkmode");
}



function createTextEditor() {
    let editor = document.getElementById("editor-container");
    if (editor !== null) {
        const toolbarOptions = [
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block', 'link', 'image', 'formula'],
            [{ 'list': 'ordered'}, { 'list': 'bullet' }, { 'list': 'check' }],
            [{ 'indent': '-1'}, { 'indent': '+1' }],
            [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
            [{ 'align': [] }],
            ['clean']
        ];


        let quill = new Quill(editor, {
            modules: {
                syntax: {hljs},
                toolbar: toolbarOptions
            },
            theme: 'bubble',
            placeholder: 'Begin your story here...',
            bounds: editor
        });
        editor.getElementsByClassName("ql-editor")[0].style.opacity = 1;
    }
    /* stuff to handle hljs on non-quill-editor pages.
    let runHljs = () => {
        if (editor === null) {
            hljs.configure({cssSelector: "pre"});
            hljs.highlightAll();
        }
    }
    try {
        if (hljs !== undefined && hljs !== null) {
            runHljs();
        }
    } catch (_) {} */
}



