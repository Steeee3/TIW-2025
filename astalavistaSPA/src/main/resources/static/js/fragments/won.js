async function renderWon(v) {
  const auctions = await api("/api/auctions/won");

  v.innerHTML = 
  `
    <section>
        <div id="wonAuctions" class="auction-section">
            <h1>Le Tue Aste Vinte</h1>
        </div>
    </section>
  `
  loadWonAuctions(auctions);
}

function loadWonAuctions(auctions) {
  const section = document.getElementById("wonAuctions");

  const htmlCards = auctions.map(auction => {
        const articlesHtml = (auction.articles || []).map(article => {
          const src = article.previewPath?.startsWith('/') ? article.previewPath : '/' + article.previewPath;
          return `
            <div class="article">
              <img src="${src}" alt="Preview articolo">
              <div class="article-details">
                <strong>${article.name}</strong>
                <p>€ ${article.price}</p>
                <p>Codice: ${article.id}</p>
              </div>
            </div>
          `;
        }).join('');

        return `
          <a href="/offer/${auction.id}" onclick="navigate('/details/${auction.id}');return false;" class="auction-card fade-in">
            <div class="auction-header">
              <h2>Lotto #${auction.id}</h2>
              <p>Scade il: ${new Date(auction.endDate).toLocaleString()}</p>
            </div>
            <div class="auction-articles">
              ${articlesHtml}
            </div>
          </a>
        `;
      }).join('');

      section.insertAdjacentHTML('beforeend', htmlCards);
}