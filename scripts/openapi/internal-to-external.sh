#!/usr/bin/env bash
    
set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
}

usage() {
      cat <<EOF
    Usage: $(basename "${BASH_SOURCE[0]}") [-h] [-v] [-t <tag>]

    [-h]                      : this help message
    [-v]                      : verbose mode
    -t <tag>                  : pn-codegen tag, default to "main"
    
EOF
  exit 1
}

parse_params() {
  # default values of variables set from params
  tag="main"

  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --verbose) set -x ;;
    -t | --tag) 
      tag="${2-}"
      shift
      ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  args=("$@")

  # check required params and arguments
  [[ -z "${tag-}" ]] && usage 
  return 0
}

dump_params(){
  echo ""
  echo "######      PARAMETERS      ######"
  echo "##################################"
  echo "Tag:          ${tag}"
}


# START SCRIPT

parse_params "$@"
dump_params

current_dir=$(dirname ${BASH_SOURCE[0]})
echo ${current_dir}
cd ${current_dir}
cd ../..
docker run --rm -it -v ${PWD}:/usr/local/app/microsvc --name=pn-codegen ghcr.io/pagopa/pn-codegen:${tag}