async function api (url, opts = {}) {
    opts.headers = Object.assign(
        {'X-XSRF-TOKEN': csrf()}, 
        opts.headers || {}
    );

    const r = await fetch(url, opts);

    if (r.status === 401) {
        window.location.assign('/login?expired=true');
        throw new Error('Unauthorized');
    }

    if (r.status === 204) return null;
    const body = await r.json().catch(() => ({}));

    if (!r.ok) throw Object.assign(
        body, 
        { status: r.status }
    );
    return body;
}

(async function bootstrap(){
  try {
    state.me = await api('/api/users/me');
    initLocalStorage(state.me.username);
  } catch (e) {
    if (e.status === 401) { location.href = '/login'; return; }
    document.getElementById('view').textContent = 'Bootstrap error (' + (e.status || 'unknown') + ')';
  }

  if (location.pathname === '/') {
    decideInitialRoute();
  } else {
    const initial = location.pathname + location.search;

    state.route = initial;
    history.replaceState({}, '', initial);
  }

  window.addEventListener("popstate", () => {
    state.route = location.pathname || "/";
    router.resolve();
  });

  router.resolve();
})();