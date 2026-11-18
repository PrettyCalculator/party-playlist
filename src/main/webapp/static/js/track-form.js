"use strict";

document.addEventListener("DOMContentLoaded", function () {
    const radios = document.querySelectorAll("input[name=sourceType]");
    const blocks = document.querySelectorAll("[data-source]");

    function update() {
        const checked = document.querySelector("input[name=sourceType]:checked");
        blocks.forEach(function (block) {
            block.hidden = checked !== null && block.dataset.source !== checked.value;
        });
    }

    radios.forEach(function (radio) {
        radio.addEventListener("change", update);
    });
    update();
});
