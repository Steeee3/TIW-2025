async function renderOffer(v, id) {
  const user = await api("/api/users/me");
  const auction = await api('/api/auctions/' + id);

  if (user["username"] === auction.seller.username) {
    navigate(`/details/${id}`);
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
  loadAuctionDetails(auction);
  loadOfferForm(v, auction);
  loadWinner(v, auction);
  loadOffers(v, auction);
}

function loadAuctionDetails(auction) {
  const auctionContainer = document.getElementById("auctionDetails");

  const details = 
  `
    <h2>Rialzo minimo: ${auction.bidStep} €</h2>
    <h3>Scade il: ${formatTimestamp(auction.endDate)}</h3>
    <h5>Venduto da: ${auction.seller.username}</h5>
  `;

  auctionContainer.insertAdjacentHTML('beforeend', details);
}

function loadOfferForm(v, auction) {
  if (auction.isClosed) {
    return;
  }

  const offerForm =
  `
    <form id="placeOfferForm" action="/placeOffer" method="post" style="text-align:center; margin-top:1rem; margin-bottom:1rem;">
        <input type="hidden" name="auctionId" value="${auction.id}" />

        <input type="number" name="offer" min="${auction.currentPrice != null ? auction.currentPrice + auction.bidStep : auction.startPrice}" step="${auction.bidStep}" placeholder="Offerta (€)"
            style="padding: 0.8rem; width: 40%; max-width: 300px; border-radius: var(--radius); border: 1px solid #ccc;" required />

        <button type="submit"
            style="padding: 0.8rem 1.2rem; background-color: var(--primary-color); color: white; border: none; border-radius: var(--radius); font-weight: bold; cursor: pointer;">
            Fai un'offerta
        </button>
    </form>
  `

  v.insertAdjacentHTML('beforeend', offerForm);
  document.getElementById('placeOfferForm').addEventListener('submit', onPlaceOffer);
}

async function onPlaceOffer(e) {
  e.preventDefault();

  const form = (e?.currentTarget instanceof HTMLFormElement)
    ? e.currentTarget
    : (e?.target instanceof Element ? e.target.closest('form') : document.getElementById('closeAuctionForm'));

  if (!(form instanceof HTMLFormElement)) {
    console.error('onPlaceOffer: form non trovato', { target: e?.target, currentTarget: e?.currentTarget });
    alert('Impossibile inviare: form non trovato.');
    return;
  }

  const fd = new FormData(e.currentTarget);

  const { token, headerName } = await ensureCsrf();
  console.log('csrf header:', headerName, 'token:', token);

  try {
    await api('/api/auctions/placeOffer', {
      method: 'POST',
      headers: { [headerName]: token },
      body: fd
    });

    form.reset();
    alert("Offerta piazzata con successo!");
    await router.resolve();
  } catch  (err) {
    const msg = err.message || err.error || err.code || "Errore nel piazzare l'offerta";
    alert(msg);
  }
}