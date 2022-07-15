cat arrivals_departures_$1.csv | tr -s $'\x01' '^' >arrivals_departures_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' arrivals_departures_$1.csv.tmp
mv arrivals_departures_$1.csv.tmp arrivals_departures_$1.csv

cat runTimesForStops_$1.csv | tr -s $'\x01' '^' >runTimesForStops_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' runTimesForStops_$1.csv.tmp
mv runTimesForStops_$1.csv.tmp runTimesForStops_$1.csv

cat runTimesForRoutes_$1.csv | tr -s $'\x01' '^' >runTimesForRoutes_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' runTimesForRoutes_$1.csv.tmp
mv runTimesForRoutes_$1.csv.tmp runTimesForRoutes_$1.csv

cat stopPaths_$1.csv | tr -s $'\x01' '^' >stopPaths_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' stopPaths_$1.csv.tmp
mv stopPaths_$1.csv.tmp stopPaths_$1.csv

cat trip_patterns_$1.csv | tr -s $'\x01' '^' >trip_patterns_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' trip_patterns_$1.csv.tmp
mv trip_patterns_$1.csv.tmp trip_patterns_$1.csv

cat trip_schedule_times_$1.csv | tr -s $'\x01' '^' > trip_schedule_times_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' trip_schedule_times_$1.csv.tmp
mv trip_schedule_times_$1.csv.tmp trip_schedule_times_$1.csv

cat trips_$1.csv | tr -s $'\x01' '^' > trips_$1.csv.tmp
sed -i 's/\\0/false/g;s/\^/true/g' trips_$1.csv.tmp
mv trips_$1.csv.tmp trips_$1.csv
