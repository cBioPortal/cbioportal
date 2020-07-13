#/bin/bash
# update the recommended docker version to install
# use like e.g. ./update_recommended_docker_version.sh 3.4.2
set -e
HELPDOC=$( cat <<EOF
Update the recommended docker version to install

Usage:

    ./update_recommended_docker_version.sh version

    e.g. ./update_recommended_docker_version.sh 3.4.2

Options:
    -h      This help documentation.
EOF
) 
 
# Halt on error
set -e

# Parse options
while getopts ":h" opt; do
    case $opt in
        h)
            echo "$HELPDOC"
            exit 0
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            echo "$HELPDOC"
            exit 1
            ;;
    esac
done

# Parse arguments
if [ "$#" -ne "1" ]
then
    echo "Invalid number of arguments: 1 needed but $# supplied" >&2
    echo "$HELPDOC"
    exit 1
fi

NEW_VERSION=$1
# strip off "v"
NEW_VERSION=${NEW_VERSION#v}
echo "Setting docs to use docker image cbioportal/cbioportal:$NEW_VERSION"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $SCRIPT_DIR/../docker

for f in $(git grep 'cbioportal/cbioportal:' | gcut -f1 -d: | uniq);
	do gsed -i "s|cbioportal/cbioportal:[a-z0-9.]*|cbioportal/cbioportal:${NEW_VERSION}|g" $f;
done
