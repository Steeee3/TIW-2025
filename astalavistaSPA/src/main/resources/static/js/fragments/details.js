async function renderDetails(v, id) {
  const user = await api("/api/users/me");
  const auction = await api('/api/auctions/' + id);

  if (user["username"] != auction.seller.username) {
    navigate(`/offer/${id}`);
  }

  v.innerHTML = `
  <div class="auction-section">

    <h1>Dettagli Lotto #${id}</h1>

    <!-- Prodotti -->
     <div id="auctionDetails" class="auction-card">
        <h2>Prodotti in Asta</h2>
        <div id="auctionArticles" class="auction-articles" style="display: block;">
            
        </div>
        <h2 id="startingPrice"></h2>
      </div>
    </div>
  `

  if (!auction.isClosed) {
    addRecentlyViewedAuction(id);
  }

  loadArticles(auction);
  loadCloseAuction(v, auction);
  loadWinner(v, auction);
  loadOffers(v, auction);
}

function loadArticles(auction) {
  const articlesDiv = document.getElementById('auctionArticles');
  const startingPriceLabel = document.getElementById('startingPrice');

  const articlesDetails = (auction.articles || []).map(article => {
    const imgs = Array.isArray(article.images) && article.images.length
      ? article.images
      : (article.previewPath ? [article.previewPath] : []);
    const datasetImages = imgs.map(s => encodeURIComponent(String(s))).join(",");
    const first = imgs[0] ? imgSrc(imgs[0]) : "";

    return `
      <div class="article full-width">
        <div class="carousel-container" data-images="${datasetImages}" data-index="0">
          ${first ? `<img src="${first}" class="carousel-img" alt="${article.name}">` : ""}
          ${imgs.length > 1 ? `
            <button class="carousel-btn prev" type="button" onclick="prevImage(this)" aria-label="Immagine precedente">&lt;</button>
            <button class="carousel-btn next" type="button" onclick="nextImage(this)" aria-label="Immagine successiva">&gt;</button>
          ` : ""}
        </div>

        <div class="article-details big-text">
          <h2>${article.name}</h2>
          <p>${article.description}</p>
          <p><strong>Prezzo di partenza: € ${Number(article.price ?? 0).toFixed(2)}</strong></p>
        </div>
      </div>
    `;
  }).join('');

  articlesDiv.insertAdjacentHTML('beforeend', articlesDetails);
  if (startingPriceLabel && auction.startPrice != null) {
    startingPriceLabel.textContent = `Prezzo di partenza: € ${Number(auction.startPrice).toFixed(2)}`;
  }
}

function loadCloseAuction(v, auction) {
  if (auction.isClosed) {
    return;
  }

  const closeForm =
  `
    <div class="auction-card">
      <h2>Azioni Asta</h2>
      <form id="closeAuctionForm" action="/closeAuction" method="post" style="text-align:center; margin-top:1rem; margin-bottom:1rem;">
        <input type="hidden" name="auctionId" value="${auction.id}" />

        <button type="submit" class="confirm-button">
            Termina Asta
        </button>
      </form>
    </div>
  `;

  v.insertAdjacentHTML('beforeend', closeForm);
  document.getElementById('closeAuctionForm').addEventListener('submit', onCloseAuction);
}

function loadWinner(v, auction) {
  if (!auction.isClosed || auction.winnerUsername == "" || auction.winnerUsername == null) {
    return
  }

  const winnerSection = 
  `
    <div class="auction-card">
      <h2>Vincitore</h2>
      <p><strong th:text="${auction.winner.name} ${auction.winner.surname}">Mario Rossi</strong></p>
      <p th:text="${auction.winner.street}, ${auction.winner.city} (${auction.winner.postalCode})">Indirizzo</p>
    </div>
  `;

  v.insertAdjacentHTML('beforeend', winnerSection);
}

function loadOffers(v, auction) {
  const offers = auction.offers;

  const offersContainerHTML = 
  `
  <!-- Offerte -->
    <div class="auction-card">
      <h2>Offerte</h2>
      <ul id="offers">
        
      </ul>
    </div>
  `;
  v.insertAdjacentHTML('beforeend', offersContainerHTML);

  const offersContainer = document.getElementById('offers');

  if (Array.isArray(offers) && offers.length === 0) {
    const noOffersHTML = 
    `
      <div>
        <p>Nessuna offerta.</p>
      </div>
    `
    offersContainer.insertAdjacentHTML('beforeend', noOffersHTML);
    return;
  }

  const offersList = offers.map(offer => {
  const offerHTML =
    `
        <li>
          <strong>${offer.user.username}</strong> -
          <span>${offer.value} €</span>
          <em>${formatTimestamp(offer.timestamp)}</em>
        </li>
    `;
    return offerHTML;
  }).join('');

  offersContainer.insertAdjacentHTML('beforeend', offersList);
}

async function onCloseAuction(e) {
  e.preventDefault();

  const form = (e?.currentTarget instanceof HTMLFormElement)
    ? e.currentTarget
    : (e?.target instanceof Element ? e.target.closest('form') : document.getElementById('closeAuctionForm'));

  if (!(form instanceof HTMLFormElement)) {
    console.error('onCloseAuction: form non trovato', { target: e?.target, currentTarget: e?.currentTarget });
    alert('Impossibile inviare: form non trovato.');
    return;
  }

  const fd = new FormData(e.currentTarget);

  const { token, headerName } = await ensureCsrf();
  console.log('csrf header:', headerName, 'token:', token);

  const res = await fetch('/api/auctions/close', {
    method: 'POST',
    headers: { [headerName]: token },
    body: fd
  });

  if (!res.ok){ 
    console.error("POST failed", res.status);
    alert('Errore nel caricamento'); 
    return; 
  }

  form.reset();
  alert("Asta terminata con successo");
  await router.resolve();
}