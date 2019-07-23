echo $PWD
# javac ./prioritization-techniques/ARTMaxMin.java
# javac ./prioritization-techniques/Genetic.java
# javac ./prioritization-techniques/GreedyTotal.java
# javac ./prioritization-techniques/GreedyAdditional.java
# javac ./prioritization-techniques/GreedyAdditionalNew.java
# javac ./prioritization-techniques/AdditionalTotal.java
# javac ./prioritization-techniques/GreedyAdditionalSimilarity.java
# mv ./prioritization-techniques/*.class ./

for (( i = 0; i < 8; i++ )); do
  for (( j = 0; j < 3; j++ )); do
    python ./scripts/scripts-py/run-priorization.py $i $j 'GreedyAdditionalSimilarity' &
  done
done

# rm ./*.class
