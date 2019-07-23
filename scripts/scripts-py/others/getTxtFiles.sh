for i in ./*/*/* ; do
  if [ -d "$i" ]; then
    IFS='/' read -a myarray <<< "$i"
    cp $i/*.txt ../data/${myarray[2]}/coverage/${myarray[3]}/
  fi
done
