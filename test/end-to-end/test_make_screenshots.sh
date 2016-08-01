#!/usr/bin/env bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'


# bash yaml parser from https://gist.github.com/epiloque/8cf512c6d64641bde388
parse_yaml() {
    local prefix=$2
    local s
    local w
    local fs
    s='[[:space:]]*'
    w='[a-zA-Z0-9_]*'
    fs="$(echo @|tr @ '\034')"
    sed -ne "s|^\($s\)\($w\)$s:$s\"\(.*\)\"$s\$|\1$fs\2$fs\3|p" \
        -e "s|^\($s\)\($w\)$s[:-]$s\(.*\)$s\$|\1$fs\2$fs\3|p" "$1" |
    awk -F"$fs" '{
    indent = length($1)/2;
    vname[indent] = $2;
    for (i in vname) {if (i > indent) {delete vname[i]}}
        if (length($3) > 0) {
            vn=""; for (i=0; i<indent; i++) {vn=(vn)(vname[i])("_")}
            printf("%s%s%s=(\"%s\")\n", "'"$prefix"'",vn, $2, $3);
        }
    }' | sed 's/_=/+=/g'
}


compare_image() {
	local python_selenium_container=${1}
    local selenium_hub_url=${2}
    local browser=${3}
    local url=${4}
    local png=${5}
    local timeout_in_seconds=${6}

    docker exec $python_selenium_container python selenium_screenshot.py \
        $selenium_hub_url $browser $url $png $timeout_in_seconds && \
        git diff --quiet -- $png
    return $?
}

upload_image() {
    local png=${1}

    curl -s -F "clbin=@$png" https://clbin.com
}

# Google URL shortener
googl () { curl -s -d "url=${1}" http://goo.gl/api/url | sed -n "s/.*:\"\([^\"]*\).*/\1\n/p" ;}

# parse config file
CONFIG_FILE=$1
eval $(parse_yaml ${CONFIG_FILE} config_)

# dir of bash script http://stackoverflow.com/questions/59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# move to test dir
cd ${DIR}

# make screenshots and compare against image in repo
screenshot_error_count=0
screenshots_failed=()
echo "Running screenshot tests"
for ((i=0; i < ${#config_screenshot_names[@]}; i++)); do
    name=${config_screenshot_names[$i]}
    url="${config_portal_url}${config_screenshot_urls[$i]}"
	timeout_in_seconds=${config_screenshot_timeout[$i]}
    max_retries=${config_screenshot_retry[$i]}

    for browser in "${config_browsers[@]}"; do
        png="screenshots/${browser}/${name}.png"
        echo -e "Checking ${DIR}/${png} at $url..."
        retry=0
        while [[ $retry -lt $max_retries ]]; do
            compare_image $config_python_selenium_container $config_selenium_hub_url \
                          $browser $url $png $timeout_in_seconds
            compare_exit_code=$?
            # break if exit code 0
            if [[ $compare_exit_code -eq 0 ]]; then
                echo -e "$GREEN SUCCESS${NC}"
                break
            else
                retry=$(($retry + 1))
                timeout_in_seconds=$((${timeout_in_seconds} + ${config_screenshot_timeout[$i]}))
                echo -e "Failure -> retry ${retry}, increase timeout to ${timeout_in_seconds}s"
            fi
        done
        # report error if failure after max_retries
        if [[ $compare_exit_code -gt 0 ]]; then
            echo -e "$RED FAILED${NC}"
            screenshot_error_count=$(($screenshot_error_count + 1))
            screenshots_failed=( ${screenshots_failed[@]} $png )
        fi
    done
done

# handle errors
if [[ $screenshot_error_count -gt 0 ]]; then
	# upload screenshots
	screenshots_uploaded=()
	for png in "${screenshots_failed[@]}"; do
		screenshots_uploaded=( ${screenshots_uploaded[@]} "$(upload_image $png)" )
	done

	# show dev how to download failing test screenshots
	echo -e "${RED}${screenshot_error_count} SCREENSHOT TESTS FAILED!${NC}"
	echo -e "FOR STEPS TO SEE IMAGE DIFF ${RED}READ CONTRIBUTING.md${NC}"
	echo "TO DOWNLOAD FAILING SCREENSHOTS TO LOCAL REPO ROOT RUN:"
	for ((i=0; i < ${#screenshots_failed[@]}; i++)); do
		png=${screenshots_failed[$i]}
		url=${screenshots_uploaded[$i]}
		echo "curl '"${url}"' > test/end-to-end/${png}"
	done

	# on travis show where to view failing screenshots online
	if [[ $TRAVIS ]]; then
		echo "OR CHECK OUT FAILED SCREENSHOTS ONLINE:"
		for ((i=0; i < ${#screenshots_failed[@]}; i++)); do
			png=${screenshots_failed[$i]}
			url=${screenshots_uploaded[$i]}
            repo_url=${TRAVIS_REPO_SLUG}/${TRAVIS_COMMIT}
			original_screenshot_url="https://raw.githubusercontent.com/${repo_url}/test/end-to-end/${png}"
			echo -e "COPY+PASTE in BROWSER TO COMPARE FAILED SCREENSHOT: ${YELLOW}http://rawgit.com/${repo_url}/test/end-to-end/image-compare/index.html?img1=${original_screenshot_url}&img2=${url}&label1=${TRAVIS_BRANCH}&label2=$(git rev-parse --short HEAD)&screenshot_name=test/end-to-end/${png}${NC}"
		done
	fi
	exit 1
else
    echo -e "${GREEN}SCREENSHOT TESTS SUCCEEDED${NC}"
    exit 0
fi
