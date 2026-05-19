// upload.js

const filePicker = document.getElementById("file-picker");
const statusText = document.getElementById("status");

if (filePicker && statusText)
{
    filePicker.addEventListener("change", function(event)
    {
        const files = event.target.files;

        if (!files || files.length === 0)
        {
            statusText.textContent = "No files selected";
            return;
        }

        const file = files[0];

        // Optional
        if (!file.name.toLowerCase().endsWith(".zip"))
        {
            statusText.textContent = "Please upload a .zip file.";
            return;
        }

        statusText.textContent = `Uploading ${files.length} file(s)...`;
        filePicker.disabled = true;

        uploadFile(file)
            .then(() => {statusText.textContent = "Upload successful!";})
            .catch(() => {statusText.textContent = "Upload failed.";})
            .finally(() => {filePicker.disabled = false;});
    });
}
else
    console.error("Error: Required elements not found.");

async function uploadFile(file)
{
    const baseUrl = "/api/modpack/zip";

    try
    {
        const fData = new FormData();

        fData.append("file", file);

        const resp = await fetch(baseUrl, {method: "POST", body: fData});
        const bodyText = await resp.text();

        if (!resp.ok)
            throw new Error("Upload failed");

        console.log("Server response:", await resp.text());
    }
    catch (errorC)
    {
        console.error("Upload error:", errorC);
        throw errorC;
    }
}