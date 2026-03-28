// upload.js

const filePicker = document.getElementById("file-picker");
const statusText = document.getElementById("status");
if (!filePicker || !statusText)
{
    console.error("Required elements not found");
    // return; // Is this return ok? Maybe change logic to flow better if needed
    // Since without the return, this should lead to an error where filePicker or statusText is null and isn't handled
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
    const baseUrl = "minecraft.com/api/upload"; // Change?
    try
    {
        const fData = new FormData();
        fData.append("file", file);

        const resp = await fetch(baseUrl,
                                        {
                                            method: "POST",
                                            body: fData
                                        }
                                );
        
        if(!resp.ok)
            throw new Error("Upload failed");

        // Helper check:
        const data = await resp.json();
        console.log("Server responde: ", data);
    }
    catch (error) {
        console.error("Upload error:", error);
        throw error;
    }
}