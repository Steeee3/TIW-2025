async function renderSell(v){
  v.innerHTML = `
    <section id="openAuctions" class="auction-section">
      <h1>Le Tue Aste Attive</h1>
      <div id="openAuctions-emptyMessage" class="empty-message" style="display:none;">
        <p>Non hai ancora aste aperte.</p>
      </div>
    </section>

    <section id="closedAuctions" class="auction-section">
      <h1 class="text-white">Le Tue Aste Chiuse</h1>
      <div id="closedAuctions-emptyMessage" class="empty-message text-white" style="display:none;">
        <p>Non hai ancora aste chiuse.</p>
      </div>
    </section>

    <section id="addArticle" class="auction-section">
      <h1>Aggiungi Un Articolo</h1>
      <div class="form-container reveal-right">
        <form id="add-article-form" class="article-form" enctype="multipart/form-data">
          <h2>Inserisci i dettagli del tuo articolo</h2>
          <input type="text" name="name" id="name" placeholder="Nome articolo" required maxlength="50"/>
          <input type="file" id="images" name="images" accept="image/*" multiple required />
          <textarea id="description" name="description" rows="4" placeholder="Descrizione..." required></textarea>
          <input type="number" name="price" id="price" placeholder="Prezzo (€)" required />
          <button type="submit">Aggiungi</button>
        </form>
      </div>
    </section>

    <section id="newAuction" class="auction-section reveal-left">
      <h1 class="text-white">Crea un'Asta</h1>
      <div id="no-articles" class="empty-message text-white" style="display:none;">
        <p>Non hai articoli disponibili. Aggiungi prima un articolo.</p>
      </div>
      <form id="new-auction-form" class="auction-form-container" style="display:none;">
        <div id="articles-grid" class="auction-articles"></div>
        <div class="auction-fields">
          <label for="endDate">Data fine asta:</label>
          <input type="datetime-local" name="endDate" id="endDate" required />
          <label for="bidStep">Incremento offerta (€):</label>
          <input type="number" name="bidStep" id="bidStep" min="1" required />
        </div>
        <div class="submit-container">
          <button type="submit" class="confirm-button">Conferma selezione</button>
        </div>
      </form>
    </section>
  `;

  document.getElementById('add-article-form').addEventListener('submit', onAddArticle);
  document.getElementById('new-auction-form').addEventListener('submit', onNewAuction);

  await Promise.all([loadOpenAuctions(), loadClosedAuctions(), loadUnsold()]);

  if (window.initReveals) {
    window.initReveals();
  } else {
    document.querySelectorAll('.reveal-left, .reveal-right, .fade-in')
            .forEach(el => el.classList.add('active'));
  }
}

async function loadUnsold(){
  const grid = document.getElementById('articles-grid');
  const none = document.getElementById('no-articles');
  const form = document.getElementById('new-auction-form');

  grid.innerHTML = '';
  none.style.display = 'none';
  form.style.display = 'none';

  const data = await api('/api/articles/unsold');
  if (!data || !data.length){ 
    none.style.display = 'block'; 
    return; 
  }
  form.style.display = 'block';

  const html = data.map(ar => {
    const id = `article_${ar.id}`;
    return `
      <div class="article">
        <input type="checkbox" id="${id}" name="selectedArticles" value="${ar.id}" class="checkbox-hidden">
        <label for="${id}">
          <img src="${imgSrc(ar.previewPath)}" alt="Preview">
          <div class="article-details">
            <strong>${ar.name}</strong>
            <p>€ ${money(ar.price)}</p>
            <p>Codice: ${ar.id}</p>
          </div>
        </label>
      </div>
    `;
  }).join('');

  grid.insertAdjacentHTML('beforeend', html);
}

async function onAddArticle(e){
  e.preventDefault();

  const form = (e?.currentTarget instanceof HTMLFormElement)
    ? e.currentTarget
    : (e?.target instanceof Element ? e.target.closest('form') : document.getElementById('add-article-form'));

  if (!(form instanceof HTMLFormElement)) {
    console.error('onAddArticle: form non trovato', { target: e?.target, currentTarget: e?.currentTarget });
    alert('Impossibile inviare: form non trovato.');
    return;
  }

  const fd = new FormData(e.currentTarget);

  const { token, headerName } = await ensureCsrf();
  console.log('csrf header:', headerName, 'token:', token);

  const res = await fetch('/api/sell/articles', {
    method: 'POST',
    headers: { [headerName]: token },
    body: fd
  });

  if (!res.ok){ 
    console.error("POST failed", res.status);
    alert('Errore nel caricamento articolo'); 
    return; 
  }
  form.reset();
  alert("Articolo caricato correttamente!");
  await Promise.all([loadUnsold(), loadOpenAuctions()]);
}

async function onNewAuction(e) {
  e.preventDefault();

  const form = e.currentTarget;
  const selected = [...form.querySelectorAll('input[name="selectedArticles"]:checked')]
    .map(x => Number(x.value));

  if (!selected.length) {
    alert('Seleziona almeno un articolo');
    return;
  }

  const payload = {
    selectedArticles: selected,
    endDate: document.getElementById('endDate').value,
    bidStep: Number(document.getElementById('bidStep').value)
  };

  const { token, headerName } = await ensureCsrf();

  const res = await fetch('/api/sell/auctions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      [headerName]: token
    },
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    console.error('POST /api/sell/auctions failed', res.status);
    alert('Errore nella creazione dell\'asta');
    return;
  }

  setLocalStorage("lastAction", "newAuction");
  form.reset();
  alert("Asta piazzata correttamente!");
  await Promise.all([loadOpenAuctions(), loadClosedAuctions(), loadUnsold()]);
}

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