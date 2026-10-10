// Minimal service worker that makes cBioPortal installable as a Progressive Web App.
// It intentionally does not cache anything or intercept requests: all traffic goes
// straight to the network, so users always get the current frontend and API data.
self.addEventListener("install", () => self.skipWaiting());
self.addEventListener("activate", (event) => event.waitUntil(self.clients.claim()));
