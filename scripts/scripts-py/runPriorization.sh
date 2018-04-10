javac ./algoritmos2priorize/ARTMaxMin.java
javac ./algoritmos2priorize/Genetic.java
javac ./algoritmos2priorize/GreedyTotal.java
javac ./algoritmos2priorize/GreedyAdditional.java
mv ./algoritmos2priorize/*.class ./

for (( i = 0; i < 8; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python ./scripts-py/run-priorization.py $i $j
  done
done

rm ./*.class
