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

# Parse options
GETOPT=/usr/local/opt/gnu-getopt/bin/getopt

! $GETOPT --test > /dev/null
if [[ ${PIPESTATUS[0]} -ne 4 ]]; then
    echo 'I’m sorry, GNU getopt is not available in this environment.'
    echo 'Install it using `brew install gnu-getopt` '
    exit 1
fi

# Short options: "-X")
OPTIONS=X
# Long options: "--before DATETIME", "--dry-run"
LONGOPTS=before:,dry-run

! PARSED=$($GETOPT --options=$OPTIONS --longoptions=$LONGOPTS --name "$0" -- "$@")
if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
    # Wrong arguments, or could not be parsed.
    exit 2
fi
eval set -- "$PARSED"

# Handle the option arguments
before=""                                       # Before date
dry_run=0                                       # Whether to do a dry-run (1) or not (0)

while true; do
    case "$1" in
        --before)
            before="$2"
            shift 2
            ;;
        -X|--dry-run)
            dry_run=1
            shift 1
            ;;
        --)
            shift
            break
            ;;
        *)
            echo "${cerr}Programming error${crst}"
            echo ""
            echo "Usage:"
            echo "  --before DATETIME       Checkout all commits before date/time"
            echo "  -X, --dry-run           Do a dry-run"
            exit 3
            ;;
    esac
done

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