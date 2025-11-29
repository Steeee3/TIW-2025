const state = { me: null, route: location.pathname, params: {} };

function decideInitialRoute() {
  const lastAction = getLocalStorageValue("lastAction");

  if (lastAction == null) {
    state.route = "/buy";
  } else if (lastAction == "newAuction") {
    state.route = "/sell";
  }

  history.replaceState({}, '', state.route);
}

function navigate (path){
    history.pushState({}, '', path);
    state.route = path;
    router.resolve();
}

const router = new Router("view")
    .addRoute("/sell/*", view => renderSell(view))
    .addRoute("/buy/*", view => renderBuy(view))
    .addRoute("/won", view => renderWon(view))
    .addRoute("/home", view => renderHome(view))
    .addRoute("/details/:id", (view, { id }) => renderDetails(view, id))
    .addRoute("/offer/:id", (view, { id }) => renderOffer(view, id))
    .setFallback(renderHome);