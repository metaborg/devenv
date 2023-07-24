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

# Short options: (none))
OPTIONS=
# Long options: "--before DATETIME"
LONGOPTS=before:

! PARSED=$($GETOPT --options=$OPTIONS --longoptions=$LONGOPTS --name "$0" -- "$@")
if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
    # Wrong arguments, or could not be parsed.
    exit 2
fi
eval set -- "$PARSED"

# Handle the option arguments
before=""                                       # Before date

while true; do
    case "$1" in
        --before)
            before="$2"
            shift 2
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
            exit 3
            ;;
    esac
done

# Get the script directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

################################################################################

repos=$(find . -type d -name .git -prune -print0 | sort -z | xargs -0)

for repo in ${repos}; do
    (
        cd "$repo/.."
        echo "Repo: ${repo}"
        branch=$(git rev-parse --abbrev-ref HEAD)
        commit=$(git rev-list -n 1 --first-parent --before="$before" "$branch")
        echo "Branch: $branch"
        echo "Commit: $commit"
        git reset --hard "$commit"
    )
done
