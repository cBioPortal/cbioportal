const fs = require('fs/promises');
const watch = require('fs').watch;
const path = require('path');

let apiTestRoot;

if (!process.env.BACKEND_ROOT) {
    console.log("YOU MUST EXPORT BACKEND_ROOT ENV VARIABLE");
    process.exit(1);
} else {
     apiTestRoot = `${process.env.BACKEND_ROOT}/test/api-e2e`;
}

async function mergeFiles(){

    const files = (await fs.readdir(`${apiTestRoot}/specs`)).map(fileName => {
        return path.join(`${apiTestRoot}/specs`, fileName);
    });

    const jsons = files.map(path => {
        return fs.readFile(path).then(data => {
            try {
                const json = JSON.parse(data);
                return { file: path, suites: json };
            } catch (ex) {
                console.log('invalid apiTest json spec');
                return [];
            }
        });
    });

    Promise.all(jsons)
        .then(d => {
            fs.writeFile(`${process.cwd()}/api-e2e/json/merged-tests.json`, JSON.stringify(d));
        })
        .then(r => console.log('merged-tests.json written'));
    
}

watch(`${apiTestRoot}/specs`, async function(event, filename) {
    if (event === 'change') {
        mergeFiles()
    }
});


mergeFiles();