async function renderBuy(v){
  v.innerHTML = `
    <section class="auction-section">
      <h1>Scopri le Aste Disponibili</h1>

      <form id="buy-search" class="search-form" style="text-align:center; margin-bottom:2rem;">
        <input id="buy-keyword" type="text" name="keyword" placeholder="Cerca per parola chiave..."
               style="padding:0.8rem; width:60%; max-width:500px; border-radius: var(--radius); border: 1px solid #ccc;" />
        <button type="submit" style="padding: 0.8rem 1.2rem; background-color: var(--primary-color); color: white; border: none; border-radius: var(--radius); font-weight: bold; cursor: pointer;">
          Cerca
        </button>
      </form>

      <div id="buy-empty" class="empty-message" style="display:none;">
        <p>Nessuna asta trovata per la parola chiave selezionata.</p>
      </div>
    </section>
  `;

  const section = v.querySelector('.auction-section');
  const form = v.querySelector('#buy-search');
  const input = v.querySelector('#buy-keyword');
  const empty = v.querySelector('#buy-empty');

  async function load(keyword){
    section.querySelectorAll('a.auction-card').forEach(n => n.remove());
    empty.style.display = 'none';

    const recentlyViewedAuctions = getAllRecentlyViewedAuctionIds().reverse();

    try{
      let data = [];
      keyword = keyword ? ('?keyword=' + encodeURIComponent(keyword)) : '';

      if (keyword == '') {
        for (let i = 0; i < recentlyViewedAuctions.length; i++) {
          data[i] = await api("/api/auctions/" + recentlyViewedAuctions[i]);
        }
      } else {
        data = await api('/api/buy' + keyword);
      }

      if (!data || data.length === 0){
        empty.style.display = 'block';
        return;
      }

      const htmlCards = data.map(auction => {
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
    }catch(e){
      if (e.status === 401){ location.href = '/login'; return; }
      empty.style.display = 'block';
      empty.textContent = 'Errore nel caricamento.';
    }
  }

  form.addEventListener('submit', (ev) => {
    ev.preventDefault();
    const kw = (input.value || '').trim();
    history.pushState({}, '', kw ? `/buy?keyword=${encodeURIComponent(kw)}` : '/buy');
    state.route = location.pathname + location.search;
    load(kw);
  });

  const params = new URLSearchParams(location.search);
  const kw = params.get('keyword') || '';
  if (kw) input.value = kw;
  load(kw);
}