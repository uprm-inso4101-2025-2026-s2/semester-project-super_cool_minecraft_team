// homepage.js

document.addEventListener("DOMContentLoaded", function(){

    // Select DOM elements
    const getStartedBtn = document.getElementById("get-mods-btn");
    const mainMessage = document.getElementById("main-message");

    // Event listener
    getStartedBtn.addEventListener("click", function(){

        // Update message
        mainMessage.textContent = "Start by uploading your modpack to analyze dependencies.";

        // Change button text after click
        getStartedBtn.textContent = "Upload Modpack";
        getStartedBtn.disabled = true;
    });
});