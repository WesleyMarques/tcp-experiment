SUBJECTS='/home/wesleynunes/Documentos/workspaceMastering/subjects2'

cd $SUBJECTS
for directory in $(find . -maxdepth 3 -mindepth 3 -type d);
do
current_dir=$(echo $directory | sed '/[a-zA-Z0-9\-]*\//{s//coverage\//3g}' | sed /[.]/{s///g})

if [ -d "$directory/target" ]; then
  python /home/wesleynunes/Documentos/workspaceMastering/tcp-experiment/scripts/scripts-py/convert-xml-json.py "$directory/target/site/jacoco/jacoco.xml" \
  && mv "./method-complexity.txt" "../tcp-experiment/data$current_dir"
fi
# cd "../tcp-experiment/data$current_dir"
cd $SUBJECTS
done
