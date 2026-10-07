# Install cBioPortal as an app

cBioPortal can be installed as a Progressive Web App (PWA) from Chrome, Chromium and other Chromium-based browsers (for example Edge). Once installed, cBioPortal opens in its own window, without browser tabs or an address bar, and gets its own icon on your taskbar, dock or app launcher.

## Install

1. Open cBioPortal in Chrome or another Chromium-based browser.
2. Click the **Install** icon at the right end of the address bar, or open the browser menu and choose **Cast, save, and share** → **Install page as app** (the wording varies between browsers and versions).
3. Confirm the prompt. cBioPortal now appears as an app on your computer.

To uninstall, open the installed app, open the menu in its title bar and choose **Uninstall**.

## Notes

* The installed app is the regular cBioPortal website in a dedicated window. It still needs a network connection, and nothing is cached for offline use, so you always see the current data and the current version of the portal.
* For server administrators: the app is described by `/manifest.json` and a small service worker served at `/service-worker.js`. Both are served relative to the portal's context path and are accessible without logging in, because browsers request them without credentials. Browsers only offer installation on pages served over HTTPS (or from `localhost`).
