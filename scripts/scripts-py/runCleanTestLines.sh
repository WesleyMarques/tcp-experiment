for (( i = 0; i < 8; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python ./scripts/scripts-py/clean_lines_of_code.py $i $j &
  done
done
