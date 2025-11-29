let container;

window.addEventListener("DOMContentLoaded", () => {
    const activeForm = document.body.getAttribute("data-active-form");
    container = document.getElementById('container')

    container.classList.add('sign-in')
    if (activeForm === "signup") {
        toggle();
    }
});
toggle = () => {
	container.classList.toggle('sign-in');
	container.classList.toggle('sign-up');
}