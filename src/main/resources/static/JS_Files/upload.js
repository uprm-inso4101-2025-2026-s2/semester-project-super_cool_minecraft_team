// upload.js

const filePicker = document.getElementById("file-picker");
const statusText = document.getElementById("status");


// if (!filePicker || !statusText)
// {
//     console.error("Required elements not found");
//     // return; // Is this return ok? Maybe change logic to flow better if needed
//     // Since without the return, this should lead to an error where filePicker or statusText is null and isn't handled
// }
if(filePicker && statusText)
{
    filePicker.addEventListener("change", function(event)
    {
        const files = event.target.files[0];

        if(files.length == 0)
        {
            statusText.textContent = "No files selected";
            return;
        }

        statusText.textContent = 'Uploading ${files.length} file(s)...';
        filePicker.disabled = true;

        uploadFile(files)
            .then(() => {statusText.textContent = "Upload successful!";})
            .catch(() => {statusText.textContent = "Upload failed.";})
            .finally(() => {filePicker.disabled = false;});
    });
}
else
    console.error("Error: Required elements not found.");

async function uploadFile(files)
{
    const baseUrl = "http://localhost:8080/upload"; //TEMPORARY

    try
    {
        const fData = new FormData();

        for(let i = 0; i < files.length; i++)
            fData.append("files", files[i]);

        const resp = await fetch(baseUrl,{method: "POST", body: fData});

        if(!resp.ok)
            throw new Error("Upload failed");

        console.log("Server response: ", await resp.json());
    }
    catch(errorC)
    {
        console.error("Upload error: ", errorC);
        throw errorC;
    }
}