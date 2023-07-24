#!/usr/bin/env bash
set -o errexit
set -o pipefail
set -o noclobber
set -o nounset
#set -o xtrace


################################################################################
# COLORS                                                                       #
################################################################################

cerr=`tput setaf 1; tput bold`     # Red and bold color
cwarn=`tput setaf 3; tput bold`    # Yellow and bold color
crst=`tput sgr0`                   # Reset color

################################################################################
# READ command-line options                                                    #
################################################################################

# Handle the option arguments
before=""                                       # Before date
dry_run=0                                       # Whether to do a dry-run (1) or not (0)

usage() {
    echo ""
    echo "Usage:"
    echo "  -b DATETIME             Checkout all commits before date/time"
    echo "  -X                      Do a dry-run"
    exit 3
}

while getopts ":b:X" o; do
    case "${o}" in
        b)
            before="$2"
            ;;
        X)
            dry_run=1
            ;;
        *)
            echo "${cerr}Programming error${crst}"
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${before}" ]; then
    usage
fi

# Get the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

################################################################################

repos=$(find . -type d -path '**/*/.git' -prune -print0 | sort -z | xargs -0)

echo "This will HARD reset all repositories, you will LOSE ALL CHANGES."
if [[ "$dry_run" -eq 0 ]]; then
    read -r -p "Are you sure? [y/N] " response
    if ! [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        dry_run=1
    fi
fi

echo "Resetting all repositories to commit before $before..."
for repo in ${repos}; do
    (
        cd "$repo/.."
        echo "${repo}"
        branch=$(git rev-parse --abbrev-ref HEAD)
        commit=$(git rev-list -n 1 --first-parent --before="$before" "$branch")
        echo "  $branch @ $commit"
        if [[ "$dry_run" -eq 0 ]]; then
            git reset --hard "$commit" --quiet
        fi
    )
done

if [[ "$dry_run" -ne 0 ]]; then
    echo "${cwarn}No changes were made because of dry-run.${crst}"
fi

echo "Done!"