for f in output*
do 
  grep -Ev "\"" $f > temp.txt
  mv temp.txt $f
  sed -i 's#{h#h#' $f
  sed -i 's#{*}##' $f
  sed -r 's/\;//g' $f
  
  sed -i 's/\([0-9]\),\(-\?[0-9]\)/\1\;\2/g' $f
  sed -i 's/\(-\?[0-9]\),\(http\)/\1\;\2/g'  $f
  awk  '{gsub(";","\t",$0); print;}' $f > temp.txt
  mv temp.txt $f
#  sed -r -i "s/,{2,}/,/g" $f
#  sed -i "s/,%/%/g" $f
done

