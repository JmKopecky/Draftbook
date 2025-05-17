function save() {
    let quill = Quill.find(document.getElementById("editor-container"));
    let html = parseHTML(quill);
    let workid = document.getElementById("return-to-work-button").getAttribute("data-work");
    let chapterid;
    for (const elem of document.getElementsByClassName("chapter-listing-li")) {
        if (elem.classList.contains("active")) {
            chapterid = elem.getAttribute("data-chapter");
            break;
        }
    }
    fetch(`/api/work/${workid}/chapter/${chapterid}/save`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            content: html,
            notes: ""
        }),
    }).then((r) => {
        r.json().then(data => {
            console.log(data);
        });
    });
}



function parseHTML(quill) {
    let html = quill.getSemanticHTML();
    html = html.replaceAll("&nbsp;", " "); //note that quill replaces all spaces with &nbsp;, which will not be ideal  for exporting raw html
    const parser = new DOMParser();
    const document = parser.parseFromString(html, "text/html");
    //fix for hljs formatting if applicable (quill editor renders correctly, raw html plays far less kindly).
    for (const elem of document.getElementsByTagName("pre")) {
        let language = elem.getAttribute("data-language");
        if (language != null) {
            elem.classList.add("language-" + language);
        }
    }
    return document.body.innerHTML;
}



function returnToWork() {
    let workid = document.getElementById("return-to-work-button").getAttribute("data-work");
    swup.navigate("/work/" + workid);
}



function toggleFocusMode() {
    let header = document.getElementById("header");
    let sidebar = document.getElementById("editor-sidebar");
    let functionbar = document.getElementById("editor-function-bar");
    if (header.style.display === "none") {
        //show
        header.style.display = "flex";
        sidebar.style.display = "flex";
        functionbar.classList.remove("focusmode");
    } else {
        //hide
        header.style.display = "none";
        sidebar.style.display = "none";
        functionbar.classList.add("focusmode");
    }
}



function openNewChapterPopup() {
    let popup = document.getElementById("chapter-new-popup");
    if (popup.style.display === "none") {
        popup.style.display = "flex";
    } else {
        popup.style.display = "none";
        popup.value = "";
    }
    //todo handle clicking out of the popup to cancel
}



function editorCreateChapter() {
    let nameInput = document.getElementById("chapter-new-name-input");
    if (nameInput.value === "") {
        nameInput.focus();
        return;
    }
    let workid = document.getElementById("return-to-work-button").getAttribute("data-work");
    let chapterNumber = 2 ** 30;
    fetch(`/api/work/${workid}/chapter/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            chaptername: nameInput.value,
            chapternumber: chapterNumber
        }),
    }).then((r) => {
        r.json().then(data => {
            if (data["error"] === "none") {
                let list = document.getElementById("chapter-list");
                const parser = new DOMParser();
                let newElem = parser.parseFromString(list.innerHTML, "text/html").body.firstElementChild;
                newElem.setAttribute("data-chapter", data["chapter_id"]);
                newElem.classList.remove("active");
                let link = newElem.getElementsByClassName("sidebar-listing")[0];
                link.textContent = data["chapter_name"];
                link.href = "/chapter/" + data["chapter_id"];
                console.log(newElem);
                list.appendChild(newElem);
                let popup = document.getElementById("chapter-new-popup");
                popup.style.display = "none";
                popup.value = "";
            } else {
                //todo replace with proper error handling
                console.log("Error encountered.");
                console.log(data["error"]);
                console.log(data);
            }
        });
    });
}