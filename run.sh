# fetch the log filenames and dates from the server and have the update perl script
# figure out if they are already here
#
# could add a check on the file size/date - only retrieve if different from the local one
# better, use rsync
#
#
# USAGE: ./run.sh user@server.com

# IF shell script - process-only, don't get new files from server
export SSH_HOST=$1
export LOG_PATH='/var/www/logs'
export LOG_FILE='access.log'

echo "Backing up older logs..."
ssh $SSH_HOST stat -f "%m%N" $LOG_PATH/* | ./perl/fetch-log.pl

echo "Downloading today's log"
scp $SSH_HOST:$LOG_PATH/$LOG_FILE ./logs/

cd ./LogParser/bin
java LogParser ../../logs/$LOG_FILE
