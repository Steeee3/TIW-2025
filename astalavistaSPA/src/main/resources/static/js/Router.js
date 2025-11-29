class Router {
    constructor (viewId) {
        this.view = document.querySelector(`#${viewId}`);
        this.routes = [];
        this.fallback = null;
    }

    addRoute(route, handler) {
        this.routes.push({ route, handler });
        return this;
    }

    setFallback(handler) {
        this.fallback = handler;
        return this;
    }

    resolve() {
        const stateRoute = this._normalize(state.route);

        for (const { route, handler } of this.routes) {
            if (this._hasParams(route)) {
                const params = this._getParamsFromPathWithPattern(route, stateRoute);

                if (params) {
                    state.params = { ...(state.params || {}), ...params };

                    return handler(this.view, params);
                }
            }

            if (this._isUniversal(route)) {
                if (this._matchesPrefix(route, stateRoute)) {
                    return handler(this.view);
                }
            }

            if (route === stateRoute) {
                return handler(this.view);
            }
        }

        return this._renderFallback(stateRoute);
    }

    _normalize(route) {
        const normalized = (route || "/").replace(/\/+$/, "");
        return normalized || "/";
    }

    _hasParams(route) {
        return route.includes("/:");
    }

    _getParamsFromPathWithPattern(route, path) {
        const [base, parameterName] = route.split("/:");

        if (!path.startsWith(base + "/")) {
            return null;
        }

        const parameterValue = decodeURIComponent(path.slice(base.length + 1));
        return { [parameterName]: parameterValue };
    }

    _isUniversal(route) {
        return route.endsWith("/*");
    }

    _matchesPrefix(route, path) {
        const base = route.slice(0, -2) || "/";
        return path === base || path.startsWith(base + "/");
    }

    _renderFallback(path) {
        if (this.fallback) {
            return this.fallback(this.view);
        }

        console.log("No route found for path: " + path);
    }
}