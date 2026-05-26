export async function getMissingDependencies() {
    const endpoint = "/api/missing-dependencies";
    
    const response = await fetch(endpoint, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!response.ok) {
        let errorMessage = "Unable to load missing dependencies.";
        try {
            const errorPayload = await response.json();
            if (errorPayload && errorPayload.message) {
                errorMessage = errorPayload.message;
            }
        } catch (error) {
            errorMessage = `Request failed with status ${response.status}.`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
}
