function setNav(elem) {
    let target = elem.getAttribute("data-section");
    if (elem.classList.contains("active")) {
        return; //element already active, do nothing
    }

    for (const tile of document.getElementsByClassName("nav-tile")) {
        tile.classList.remove("active");
    }
    elem.classList.add("active");

    for (const tile of document.getElementsByClassName("contentarea")) {
        if (tile.getAttribute("data-section") === target) {
            tile.style.display = "flex";
        } else {
            tile.style.display = "none";
        }
    }

    //change history url to reflect new section
    window.history.replaceState({section: target},
        "Work Display Section", window.location.href.split("?")[0] + "?section=" + target);
}



function createChapter() {
    let nameInput = document.getElementById("chapter-create-title-input");
    if (nameInput.value === "") {
        nameInput.focus();
        return;
    }
    let chapterNumberInput = document.getElementById("chapter-create-number-input");
    if (chapterNumberInput.value === "") {
        chapterNumberInput.focus();
        return;
    }
    let workId = document.getElementById("workid").getAttribute("data-workid");
    fetch(`/api/work/${workId}/chapter/create`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            chaptername: nameInput.value,
            chapternumber: chapterNumberInput.value
        }),
    }).then((r) => {
        r.json().then(data => {
            let chapterId = data["chapter_id"];
            let chapterName = data["chapter_name"];
            let newChapterListing =
                `
                <div class="chapter-listing" data-chapter="${chapterId}">
                    <div class="chapter-move-icons">
                        <i class="fa-solid fa-up-long"></i>
                        <i class="fa-solid fa-down-long"></i>
                    </div>
                    <form onsubmit="renameChapter(this); return false;">
                        <input class="chapter-name-input" value="${chapterName}" readonly>
                    </form>
                    <div class="chapter-modification-icons">
                        <i class="fa-solid fa-i-cursor" onclick="renameChapter(this)"></i>
                        <i class="fa-solid fa-trash" onclick="deleteChapter(this)"></i>
                    </div>
                    <button onclick="openChapter(this)">Edit</button>
                </div>
                `
            document.getElementById("chapter-list-area").innerHTML += newChapterListing;
        });
    });
}


function renameChapter(elem) {
    let chapterContainer = elem.parentElement;
    if (!chapterContainer.classList.contains("chapter-listing")) {
        chapterContainer = chapterContainer.parentElement;
    }

    let chapterId = chapterContainer.getAttribute("data-chapter");
    let workId = document.getElementById("workid").getAttribute("data-workid");
    let input = chapterContainer.getElementsByTagName("form")[0].getElementsByTagName("input")[0];
    if (input.readOnly === true) {
        input.readOnly = false;
        return;
    }
    if (input.value === "") {
        input.focus();
        return;
    }
    fetch(`/api/work/${workId}/chapter/${chapterId}/rename`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            newname:  input.value
        }),
    }).then((r) => {
        r.json().then(data => {
            console.log(data);
            input.readOnly = true;
        });
    });
}



function deleteChapter(elem) {
    let chapterContainer = elem.parentElement.parentElement;
    let chapterId = chapterContainer.getAttribute("data-chapter");
    let workId = document.getElementById("workid").getAttribute("data-workid");
    fetch(`/api/work/${workId}/chapter/${chapterId}/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            empty: true
        }),
    }).then((r) => {
        r.json().then(data => {
            if (data["error"] === "none") {
                chapterContainer.remove();
            }
        });
    });
}


function openChapter(elem) {
    let chapterContainer = elem.parentElement;
    let chapterId = chapterContainer.getAttribute("data-chapter");
    swup.navigate(`/chapter/${chapterId}`);
}