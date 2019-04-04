for (( i = 0; i < 3; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python $PWD/scripts/scripts-py/gen_matrix_of_faults.py $i $j $1
  done
done
