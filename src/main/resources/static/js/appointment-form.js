document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('appointmentForm');
    if (!form) { return; }
    form.addEventListener('submit', function () {
        var button = form.querySelector('button[type=submit]');
        // Disable after the submit has started, so the form data is still sent
        setTimeout(function () { button.disabled = true; button.textContent = 'Sending...'; }, 0);
    });
});