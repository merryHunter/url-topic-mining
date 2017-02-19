for f in output*
do 
  grep -Ev "\"" $f > temp.txt
  mv temp.txt $f
  sed -i 's#{h#h#' $f
  sed -i 's#{*}##' $f
done

