#!/bin/sh
for (( i = 0; i < 8; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python $PWD/scripts/scripts-py/collect_projects_info.py $i $j
  done
done
