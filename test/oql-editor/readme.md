1. Copy package.json to cbioportal root.
2. Ensure nodejs is installed.
3. Run `npm install` to install required node modules.
4. Ensure cBioPortal is running on the machine. Default URL is `http://localhost:8080/cbioportal/`. Change URL if necessary in line 1 of test.js
5. Run `casperjs test test.js` to run tests
