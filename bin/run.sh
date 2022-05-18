#!/bin/bash

function usage() {
  echo "Usage: bash run.sh -n <name> -dt <date_time> [-q <yarn_queue>]"
  echo "Options: "
  echo "  -n, --name         <Required>  Job name(class name)"
  echo "  -d, --date         <Required>  date time"
  echo "  -q, --queue        [Optional]  yarn queue"
  echo "      --instances    [Optional]  num-executors"
  echo "      --parallelism  [Optional]  spark.default.parallelism"
  exit 0
}

function tips() {
  echo "run.sh: $1 not set!"
  echo "run.sh: try 'run.sh -h' or 'run.sh --help' for more information."
  exit 0
}

ARGS=$(getopt -a -o n:d:q:h -l name:,date:,queue:,instances:,parallelism:,help -- "$@")
eval set -- "${ARGS}"
while true; do
  case "$1" in
  -n | --name)
    JOB_NAME="$2"
    shift
    ;;
  -d | --date)
    P_DATE="$2"
    shift
    ;;
  -q | --queue)
    YARN_QUEUE="$2"
    shift
    ;;
  --instances)
    INSTANCES="$2"
    shift
    ;;
  --parallelism)
    PARALLELISM="$2"
    shift
    ;;
  -h | --help)
    usage
    ;;
  --)
    shift
    break
    ;;
  esac
  shift
done

function check_args() {
  item="$1"
  arg_name=${item%%:*}
  arg_default=${item#*:}
  if [[ "$arg_name" == "$arg_default" ]]; then
    arg_default=""
  fi
  if [ -z "$(eval echo "$"$arg_name)" ]; then
    eval $arg_name="$arg_default"
  fi
}
t1Day=$(date -d "1 day ago" +%Y%m%d)
items=("P_DATE:${t1Day}" YARN_QUEUE:default INSTANCES:10 PARALLELISM:10)

for item in "${items[@]}"; do
  check_args "$item"
done

if [ -z "$JOB_NAME" ]; then
  tips "JOB_NAME"
fi

cmd="spark-submit @@\
  --master yarn @@\
  --deploy-mode cluster @@\
  --driver-cores 2 @@\
  --driver-memory 2g @@\
  --num-executors ${INSTANCES} @@\
  --executor-cores 1 @@\
  --executor-memory 4g @@\
  --queue ${YARN_QUEUE} @@\
  --conf spark.default.parallelism=${PARALLELISM} @@\
  --conf spark.sql.shuffle.partitions=${PARALLELISM} @@\
  --conf spark.yarn.maxAppAttempts=1 @@\
  --conf spark.sql.files.mergeSmallFile.enabled=true @@\
  --conf spark.sql.files.mergeSmallFile.maxBytes=268435456 @@\
  --class ${MAIN_CLASS} @@\
  ${JAR_PATH} @@\
  --conf demo.job.name=${JOB_NAME} @@\
  --conf demo.partition.date=${P_DATE}"

echo -e "+++ SPARK-SUBMIT +++\n${cmd//@@/\\\\\n}\n++++++++++++++++++++"

read -r -p "Please check SPARK-SUBMIT cmd. Are You Sure? [Y/n]" input
case $input in
[yY][eE][sS] | [yY])
  nohup bash -c "${cmd//@@/}" >./"${JOB_NAME}"_"${P_DATE}".log 2>&1 &
  ;;
[nN][oO] | [nN])
  exit 0
  ;;
*)
  echo "Invalid input ..."
  exit 1
  ;;
esac
