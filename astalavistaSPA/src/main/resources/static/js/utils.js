function imgSrc(p){ return p?.startsWith('/') ? p : ('/' + (p || 'images/placeholder.png')); }
function money(n){ return (Math.round(Number(n)*100)/100).toFixed(2); }
function fmtDate(iso){ const d = new Date(iso); if (isNaN(d)) return iso; const z=n=>String(n).padStart(2,'0'); return `${z(d.getDate())}/${z(d.getMonth()+1)}/${d.getFullYear()} ${z(d.getHours())}:${z(d.getMinutes())}`; }

function csrf() {
  const m = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : null;
}

async function ensureCsrf() {
  const r = await fetch('/api/csrf', { cache: 'no-store' });
  if (!r.ok) throw new Error('Cannot get CSRF');
  const { token, headerName } = await r.json();
  return { token, headerName };
}

async function logout() {
  try {
    const { token, headerName } = await ensureCsrf();
    await fetch('/logout', {
      method: 'POST',
      headers: { [headerName]: token },
      credentials: 'same-origin'
    });
  } catch (e) {
    console.error('Logout failed:', e);
  } finally {
    
    window.location.assign('/login?logout=true');
  }
}

const formatTimestamp = (ts) => {
  if (!ts) return '';
  const d = ts instanceof Date ? ts : new Date(ts);
  if (Number.isNaN(d.getTime())) return '';
  const parts = new Intl.DateTimeFormat('it-IT', {
    timeZone: 'Europe/Rome',
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false
  }).formatToParts(d);
  const get = t => parts.find(p => p.type === t)?.value ?? '';
  return `${get('day')}/${get('month')}/${get('year')} ${get('hour')}:${get('minute')}:${get('second')}`;
};

function toSrc(p) {
  let s = String(p || '');
  if (!s) return s;
  if (/^https?:\/\//i.test(s)) return s;
  s = s.replace(/^\/+/, '');
  return '/' + encodeURI(s);
}

window.nextImage = function (button) {
  const c = button.closest(".carousel-container");
  const images = (c.dataset.images || "")
    .split(",").map(decodeURIComponent).map(s => s.trim()).filter(Boolean);
  if (!images.length) return;

  const img = c.querySelector(".carousel-img");
  let idx = Number(c.dataset.index || "0");
  idx = (idx + 1) % images.length;
  c.dataset.index = String(idx);
  img.src = toSrc(images[idx]);
};

window.prevImage = function (button) {
  const c = button.closest(".carousel-container");
  const images = (c.dataset.images || "")
    .split(",").map(decodeURIComponent).map(s => s.trim()).filter(Boolean);
  if (!images.length) return;

  const img = c.querySelector(".carousel-img");
  let idx = Number(c.dataset.index || "0");
  idx = (idx - 1 + images.length) % images.length;
  c.dataset.index = String(idx);
  img.src = toSrc(images[idx]);
};

async function loadOpenAuctions() {
    const section = document.getElementById("openAuctions");
    const emptyMessage = document.getElementById("openAuctions-emptyMessage");

    section.querySelectorAll('a.auction-card').forEach(n => n.remove());
    emptyMessage.style.display = 'none';

    const data = await api('/api/auctions/open');
    if (!data || !data.length){
        emptyMessage.style.display='block';
        return;
    }

    const html = createAuctionCards(data);
    section.insertAdjacentHTML('beforeend', html);
}

async function loadClosedAuctions() {
    const section = document.getElementById("closedAuctions");
    const emptyMessage = document.getElementById("closedAuctions-emptyMessage");

    section.querySelectorAll('a.auction-card').forEach(n => n.remove());
    emptyMessage.style.display = 'none';

    const data = await api('/api/auctions/closed');
    if (!data || !data.length){
        emptyMessage.style.display='block';
        return;
    }

    const html = createAuctionCards(data);
    section.insertAdjacentHTML('beforeend', html);
}

function createAuctionCards(auctions) {
  return auctions.map(a => {
    const arts = (a.articles || []).map(ar => `
      <div class="article">
        <img src="${imgSrc(ar.previewPath)}" alt="Preview">
        <div class="article-details">
          <strong>${ar.name}</strong>
          <p>€ ${money(ar.price)}</p>
          <p>Codice: ${ar.id}</p>
        </div>
      </div>
    `).join('');

    const current = a.currentPrice ? `<p>Offerta attuale: €${money(a.currentPrice)}</p>` : '';
    const seller  = a.sellerUsername ? `<p>Venditore: ${a.sellerUsername}</p>` : '';
    const remain  = a.remainingSeconds != null
        ? `<p>Tempo rimasto: ${Math.floor(a.remainingSeconds/3600)}h</p>`
        : '';

    return `
      <a href="${a.detailUrl}" class="auction-card fade-in"
         onclick="event.preventDefault(); navigate('${a.detailUrl}');">
        <div class="auction-header">
          <h2>Lotto #${a.id}</h2>
          <p>Scade il: ${fmtDate(a.endDate)}</p>
          <p>Prezzo iniziale: €${money(a.startPrice)}</p>
          ${current}
          ${seller}
          ${remain}
        </div>
        <div class="auction-articles">${arts}</div>
      </a>
    `;
  }).join('');
}