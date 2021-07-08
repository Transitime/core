#!/bin/bash
#############################################################
# This script archives old data from database into Amazon AWS
# Glacier for inexpensive long-term storage.
#############################################################

# Command line arguments
agencyId=$1
daysOld=$2
dbHost=$3
dbUser=$4
dbPassword=$5
logDir=$6

# Some constants
tmpDir="dataTmpDir"
dataTarFile="$tmpDir/data.tar"

# Logs info along with timestamp
function log() {
  echo
  echo `date +"%D %T"` $1
}

# Performs an SQL query. If there is an error then exits
# $1 is the query to be performed.
function query() {
  query=$1

  export PGPASSWORD=$dbPassword
  psqlCommand="psql -h $dbHost -d $agencyId -U $dbUser --no-password -c $query"
  log "EXECUTING: $psqlCommand"
  eval "$psqlCommand"
  if [ $? != 0 ]; then
        log "Query failed. Exiting!"
        exit -1
  fi
}

# Executes the query and writes data to file $tmpDir/$1.csv .
# Then purges that data from the db table and does 
# vacuum full without locking up the table by using a series
# of temp tables.
# $1 is the name of db table. 
# $2 is the WHERE sql clause that specifies which data to archive.
function queryIntoFile() {
  tableName=$1
  whereClause=$2

  fileName="$tmpDir/$tableName"".csv"

  # Retrieve data from db and put into local file
  query "\"copy (SELECT * FROM $tableName $whereClause) to stdout with csv header\" > $fileName"

  # Compress the local data file
  log "Compressing file $fileName"
  gzip $fileName

  # Delete the old data now that it has been stored to a file
  query "\"DELETE FROM $tableName $whereClause\""

  # Reclaim disk space now that data has been deleted from the table.
  # Key thing is that the table will be locked when "vacuum full" is
  # done. Therefore:
  #   1) rename "table" to "table_for_vacuuming". 
  #   2) create a temp "table" so that system can continue to write new data
  #   3) do vacuum full on "table_for_vacuuming"
  #   4) rename "table" (the table with new data) to "table_new_data"
  #   5) rename vacuumed "table_for_vacuuming" to "table"
  #   6) copy from "table_new_data" into "table" to get data from when vacuuming
  #   7) delete "table_new_data"
  #   Done!
  tableForVacuumingName=$tableName"_for_vacuuming"
  tableForNewDataName=$tableName"_new_data"
  
  # First, rename "table" to "table_for_vacuuming"
  query "\"ALTER TABLE $tableName RENAME TO $tableForVacuumingName\""

  # 2) create a temp "table" so that system can continue to write new data 
  query "\"CREATE TABLE $tableName (LIKE $tableForVacuumingName)\""
  
  # 3) do vacuum full on "table_for_vacuuming"
  query "\"VACUUM FULL $tableForVacuumingName\""

  # 4) rename "table" (the table with new data) to "table_new_data"
  query "\"ALTER TABLE $tableName RENAME TO $tableForNewDataName\""

  # 5) rename vacuumed "table_for_vacuuming" to "table"
  query "\"ALTER TABLE $tableForVacuumingName RENAME TO $tableName\""

  # 6) copy from "table_new_data" into "table" to get data from when vacuuming
  query "\"INSERT INTO $tableName (SELECT * FROM $tableForNewDataName)\""

  #   7) delete "table_new_data"
  query "\"DROP TABLE $tableForNewDataName\""
}

####################### MAIN CODE ###################

# Create temp directory to put data files.
# If directory already exists then first erase it.
if [ -e $tmpDir ]; then
	rm -r $tmpDir
fi

mkdir $tmpDir
if [ ! -e $tmpDir ]; then
	log "tmpDir could not be created"
	exit -1
fi 

# Determine cutoff date for the data. If current date/time
# is 1/14/2015 and it is 3:30am and data older than 1 day
# should be archived then remove data for time<'1/13/2015'.
# Note that this means only data before 1/13/2015 will be
# deleted. Data for 1/13/2015 itself will remain.
cutoffDate=`date -d "\`date +%D\` $daysOld days ago" +%Y-%m-%d`

# Write old data to files and remove it from db
queryIntoFile avlReports "WHERE time < '$cutoffDate'"
queryIntoFile arrivalsDepartures "WHERE time < '$cutoffDate'"
queryIntoFile matches "WHERE avltime < '$cutoffDate'"
queryIntoFile predictionAccuracy "WHERE arrivalDepartureTime < '$cutoffDate'"
queryIntoFile predictions "WHERE creationTime < '$cutoffDate'"
queryIntoFile vehicleEvents "WHERE time < '$cutoffDate'"

# tar all of the data files together into file called data.tar
log "Tar'ing all the files in $tmpDir"
tar --remove-files -cvf $dataTarFile $tmpDir/* 
echo
echo "tar file is: "`ls -sh $dataTarFile`

# Copy data file to Amazon AWS Glacier for long-term storage
log "Copying data file to Amazon AWS Glacier"
archiveDescription=$cutoffDate
vaultName="$agencyId-dbData"
region="us-west-2"
java -Dlogback.configurationFile=/home/ec2-user/config/logbackStdout.xml -cp "/home/ec2-user/jars/*" org.transitclock.maintenance.AwsGlacierArchiver $dataTarFile $archiveDescription $region $vaultName $logDir
if [ $? != 0 ]; then
        log "Archiving tar file to AWS Glacier failed. Exiting!"
        exit -1
fi


# Remove the data file now that it has been archived
log "Removing data file"
rm -r $tmpDir

log "Done"
