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
    let workId = document.getElementById("workid").textContent.split(" ").findLast(() => true);
    fetch("/api/works/chapters/create?target=" + workId, {
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
            console.log(data);
        });
    });
}