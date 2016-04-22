#!/usr/bin/env bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

compare_image() {
    local url=${1}
    local png=${2}
    local TIMEOUT=10000

    phantomjs --ignore-ssl-errors=true make_screenshot.js $url $png $TIMEOUT && \
        git diff --quiet -- $png
    return $?
}

upload_image() {
    local png=${1}

    curl -s -F "clbin=@$png" https://clbin.com
}

# screenshots and the urls where they were taken
declare -A screenshot_urls=(
    ["screenshots/patient_view_lgg_ucsf_2014_case_id_P04.png"]="http://localhost:8080/case.do?cancer_study_id=lgg_ucsf_2014&case_id=P04"
    ["screenshots/study_view_lgg_ucsf_2014.png"]="http://localhost:8080/study.do?cancer_study_id=lgg_ucsf_2014"
)

# dir of bash script http://stackoverflow.com/questions/59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# move to test dir
cd ${DIR}

# make screenshots and compare against image in repo
screenshot_error_count=0
screenshots_failed=()
echo "Running screenshot tests"
for png in "${!screenshot_urls[@]}"; do
    url=${screenshot_urls[$png]}
    echo -e "Checking ${DIR}/${png} at $url..."
    compare_image $url $png
    # check exit code
    if [[ $? -gt 0 ]]; then
        echo -e "$RED FAILED${NC}"
        screenshot_error_count=$(($screenshot_error_count + 1))
        screenshots_failed=( ${screenshots_failed[$@]} $png )
    else
        echo -e "$GREEN SUCCESS${NC}"
    fi
done

# show dev how to download failing test screenshots
if [[ $screenshot_error_count -gt 0 ]]; then
    echo -e "${RED}${screenshot_error_count} SCREENSHOT TESTS FAILED!${NC}"
    echo -e "FOR STEPS TO SEE IMAGE DIFF ${RED}READ CONTRIBUTING.md${NC}"
    echo "TO DOWNLOAD FAILING SCREENSHOTS TO LOCAL REPO ROOT RUN:"
    for png in "${screenshots_failed[$@]}"; do
        echo "curl '"$(upload_image $png)"' > test/end-to-end/${png}"
    done
    exit 1
else
    echo -e "${GREEN}SCREENSHOT TESTS SUCCEEDED${NC}"
    exit 0
fi
