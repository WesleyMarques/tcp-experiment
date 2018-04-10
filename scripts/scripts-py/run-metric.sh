for (( i = 0; i < 8; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python ./scripts-py/genMatrixOfFaults.py $i $j $1
  done
done
