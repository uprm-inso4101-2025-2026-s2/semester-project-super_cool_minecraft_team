// upload.js

const filePicker = document.getElementById("file-picker");
const statusText = document.getElementById("status");
if (!filePicker || !statusText) {
    console.error("Required elements not found");
    return;
}
filePicker.addEventListener("change", function(event) {
    const file = event.target.files[0];
    if (!file) {
        statusText.textContent = "No file selected.";
        return;
    }
    if (!file.type.includes("zip") && !file.name.endsWith(".zip")) {
        statusText.textContent = "Please upload a valid .zip file.";
        return;
    }
    statusText.textContent = "Uploading...";
    filePicker.disabled = true;
    uploadFile(file)
        .then(() => {
            statusText.textContent = "Upload successful!";
        })
        .catch(() => {
            statusText.textContent = "Upload failed.";
        })
        .finally(() => {
            filePicker.disabled = false;
        });
});

async function uploadFile(file) {
    const baseUrl = "minecraft.com/api/upload";
    try {
        //  TODO
    }
    catch (error) {
        console.error("Upload error:", error);
        throw error;
    }
}