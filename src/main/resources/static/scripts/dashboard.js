function setNavPanel(clicked) {

    let target = clicked.getAttribute("data-section");

    //handle nav highlighting
    if (clicked.classList.contains("active")) {
        //we are already at this panel, do nothing.
        return;
    }
    for (const elem of document.getElementsByClassName("nav-item")) {
        elem.classList.remove("active");
    }
    clicked.classList.add("active");

    //handle active elements
    let elements = [];
    Array.from(document.getElementsByClassName("content-header")).forEach((e) => elements.push(e))
    Array.from(document.getElementsByClassName("content-tile")).forEach((e) => elements.push(e));
    for (const elem of elements) {
        if (elem.getAttribute("data-section") === target) {
            elem.style.display = "flex";
        } else {
            elem.style.display = "none";
        }
    }

    //change history url to reflect new section
    window.history.replaceState({section: target},
        "Dashboard Section", window.location.href.split("?")[0] + "?section=" + target);
}


function createWork() {
    let inputElem = document.getElementById("new-work-title-input");
    if (inputElem.value === "") {
        inputElem.focus();
        return;
    }

    fetch("/api/work/create", {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            title: inputElem.value
        }),
    }).then((r) => {
        r.json().then(data => {
            if (data["workid"] !== undefined) {
                let newWorkId = data["workid"];
                let newWorkTitle = data["workname"];
                let newElemString =
                    `
                    <div class="content-tile work-display-tile" data-section="works" style="display: flex;"  data-work="${newWorkId}">
                        <form onsubmit="renameWork(this); return false;"><input class="work-title" value="${newWorkTitle}" readonly></form>
                        <i class="fa-solid fa-i-cursor" onclick="renameWork(this)"></i>
                        <i class="fa-solid fa-trash" onclick="deleteWork(this)"></i>
                        <button onclick="openWork(this)">Open Work</button>
                    </div>
                    `
                document.getElementById("work-list-container").innerHTML += newElemString;
            }
        });
    });
}



function renameWork(elem) {
    let workElem = elem.parentElement;
    let workNameInput = workElem.getElementsByTagName("form")[0].getElementsByTagName("input")[0];
    if (workNameInput.readOnly) {
        workNameInput.readOnly = false;
        return;
    }

    if (workNameInput.value === "") {
        workNameInput.focus();
        return;
    }

    fetch(`/api/work/${workElem.getAttribute("data-work")}/rename`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            newName: workNameInput.value
        }),
    }).then((r) => {
        r.json().then(data => {
            workNameInput.readOnly = true;
        });
    });
}



function deleteWork(elem) {
    let workElem = elem.parentElement;

    fetch(`/api/work/${workElem.getAttribute("data-work")}/delete`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            "empty": true
        }),
    }).then((r) => {
        r.json().then(data => {
            workElem.remove();
        });
    });
}



function openWork(elem) {
    let workid = elem.parentElement.getAttribute("data-work");

    swup.navigate("/work/" + workid);
}