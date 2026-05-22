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
        if (!file.name.endsWith(".zip")) {
            statusText.textContent = "Please upload a .zip file.";
            return;
        }

        statusText.textContent = `Uploading ${files.length} file(s)...`;
        filePicker.disabled = true;

        uploadFile(file)
            .then(() => 
            {
                statusText.textContent = "Upload successful!";
            })
            .catch(() => {statusText.textContent = "Upload failed.";})
            .finally(() => {filePicker.disabled = false;});
    });
}
else
    console.error("Error: Required elements not found.");

async function uploadFile(file)
{
    const uploadURl = "/api/modpack/zip"

    try
    {
        const fData = new FormData();

        fData.append("file", file);

        const resp = await fetch(uploadURl,{method:"POST",body: fData});
        const bodyText = await resp.text();

        if (!resp.ok)
            throw new Error("Upload failed");

        console.log("Server response:", await resp.text());
        let payload = {};
        try {
            payload = bodyText ? JSON.parse(bodyText) : {};
        }catch (_) {
            /* non-JSON success body: still treat as success if status was 2xx */
        }

        console.log("Server response:",bodyText);

        const nextURL = typeof payload.redirect === "string" ? payload.redirect : "/graph";
        window.location.assign(nextURL);
    }
    catch (errorC)
    {
        console.error("Upload error:", errorC);
        throw errorC;
    }
}