#!/bin/bash

#####################################################################
# This script archives old Tomcat logfiles into Amazon AWS Glacier
# for inexpensive long-term storage.
#####################################################################

# Command line arguments
daysOld=$1

# Some constants
tomcatLogDir="/usr/share/tomcat7/logs/"
region="us-west-2"
zipFile="$tomcatLogDir/tomcatLogfiles.zip"
logDir="/home/ec2-user/logs/website"

# Logs info along with timestamp
function log() {
  echo
  echo `date +"%D %T"` $1
}

# Actually archives the data.
# Uses globals $daysOld, $zipFile, $region, and $logDir .
function archive() {
  # Determine cutoff date for the data. If current date/time
  # is 1/14/2015 and it is 3:30am and data older than 1 day
  # should be archived then remove data for time<'1/13/2015'.
  # Note that this means only data before 1/13/2015 will be
  # deleted. Data for 1/13/2015 itself will remain.
  cutoffDate=`date -d "\`date +%D\` $daysOld days ago" +%Y-%m-%d`

  log "Copying file to Amazon AWS Glacier"
  archiveDescription="up to $cutoffDate"
  vaultName="tomcat-logs"
  command="java -Dlogback.configurationFile=/home/ec2-user/config/logbackStdout.xml -cp \"/home/ec2-user/jars/*\" org.transitclock.maintenance.AwsGlacierArchiver $zipFile \"$archiveDescription\" $region $vaultName $logDir"
  log "Executing: $command"
  eval "$command"
  if [ $? != 0 ]; then
        log "Archiving tar file to AWS Glacier failed. Exiting!"
        exit -1
  fi
}

# Zip the old files together so that we have a single file that is
# compressed. The zip --move option removes the files that were
# included in the zip. By cd into the directory first the file names
# in the zip file will not have the full directory name, which makes
# them easier to use when unzipping.
log "Putting old logs into zip file"
cd $tomcatLogDir
find . -mtime +$daysOld | sudo zip --move $zipFile -@
echo "zip file is: "`ls -sh $zipFile`

# Copy file to Amazon AWS Glacier for long-term storage
archive

# Cleanup
log "Removing zipfile"
sudo rm $zipFile
