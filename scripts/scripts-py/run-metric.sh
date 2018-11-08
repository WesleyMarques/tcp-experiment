for (( i = 0; i < 7; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python $PWD/scripts/scripts-py/genMatrixOfFaults.py $i $j $1
  done
done
