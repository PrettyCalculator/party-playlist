"use strict";

document.addEventListener("submit", function (event) {
    const form = event.target;

    if (form.dataset.confirm && !confirm(form.dataset.confirm)) {
        event.preventDefault();
        return;
    }

    form.querySelectorAll("button").forEach(function (button) {
        button.disabled = true;
    });
});

window.addEventListener("pageshow", function () {
    document.querySelectorAll("button").forEach(function (button) {
        button.disabled = false;
    });
});
