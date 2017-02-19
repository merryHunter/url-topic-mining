for f in output*
do
  mongoimport --db $1 --collection $2 --type csv --file $f --fields lat,lon,urls
done
