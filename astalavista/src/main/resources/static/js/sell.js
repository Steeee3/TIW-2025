function checkImageLimit(input) {
  const maxFiles = 5;
  if (input.files.length > maxFiles) {
    alert("Puoi selezionare al massimo 5 immagini.");
    
    const dt = new DataTransfer();
    for (let i = 0; i < maxFiles; i++) {
      dt.items.add(input.files[i]);
    }
    
    input.files = dt.files;
  }
}

document.addEventListener("DOMContentLoaded", () => {
    const revealsRight = document.querySelectorAll(".reveal-right");
    const revealsLeft = document.querySelectorAll(".reveal-left");

    const appearOnScroll = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add("active");
            }
        });
    }, {
        threshold: 0.1
    });

    revealsRight.forEach(el => {
        appearOnScroll.observe(el);
    });
    revealsLeft.forEach(el => {
        appearOnScroll.observe(el);
    });
});