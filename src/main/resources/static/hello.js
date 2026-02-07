// hello.js — simplified counter for learners
// This version favors readability and common beginner patterns.

// Wait for DOM ready so elements exist before we query them.
document.addEventListener('DOMContentLoaded', function () {
    // Use the simpler getElementById API for direct access to elements.
    const button = document.getElementById('counter-button');
    const valueEl = document.getElementById('counter-value');

    // If the elements aren't present (unexpected), log and stop to avoid runtime errors.
    if (!button || !valueEl) {
        // eslint-disable-next-line no-console
        console.warn('Counter demo elements not found.');
        return;
    }

    // Simple numeric state stored in a local variable.
    let count = 0;

    // Single responsibility: update the visible value.
    function update() {
        // Only change textContent to avoid changing the DOM structure.
        valueEl.textContent = String(count);
    }

    // Attach behavior: increment on click. Native buttons already support keyboard activation.
    button.addEventListener('click', function () {
        count += 1;
        update();
    });

    // Render the initial value.
    update();
});
