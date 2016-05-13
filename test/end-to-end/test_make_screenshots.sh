#!/usr/bin/env bash
# halt on error
set -e

# make sure screenshot is still the same as the one in the repo, if not upload
# the image
upload_screenshot_if_different() {
    git diff --quiet -- $1 || \
        (echo "screenshot differs see:" && curl -F "clbin=@$1" https://clbin.com && exit 1)
}

# dir of bash script http://stackoverflow.com/questions/59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# patient view screenshot
phantomjs --ignore-ssl-errors=true ${DIR}/make_screenshot.js 'http://localhost:8080/case.do?cancer_study_id=lgg_ucsf_2014&case_id=P04' \
                              "${DIR}/screenshots/patient_view_lgg_ucsf_2014_case_id_P04.png" \
                              5000
upload_screenshot_if_different ${DIR}/screenshots/patient_view_lgg_ucsf_2014_case_id_P04.png

# studie view screenshot
phantomjs --ignore-ssl-errors=true --web-security=false ${DIR}/make_screenshot.js 'http://localhost:8080/study.do?cancer_study_id=lgg_ucsf_2014' \
                              "${DIR}/screenshots/study_view_lgg_ucsf_2014.png" \
                              5000
upload_screenshot_if_different ${DIR}/screenshots/study_view_lgg_ucsf_2014.png
