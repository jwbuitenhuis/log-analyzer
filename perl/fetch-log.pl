#!/usr/bin/perl -w

use strict;
use POSIX 'strftime';

my ($SSH_HOST, $LOG_PATH, $LOG_FILE) = @ARGV;
print STDERR $SSH_HOST . $LOG_PATH . $LOG_FILE;
exit;
my @result =
	grep { $_ }  # filter the entries where something happened
	map { processLine($_) }
	grep { $_ =~ m|\.gz$| } <>; # only the historic log files

print STDERR scalar(@result) . " files retrieved\n";


sub processLine {
	my $line = shift;

	chomp $line;

	my ($epoch, $path) = $line =~ m|(\d+)(.*)|is;
	my ($filename) = $path =~ m|/([^/]*)$|;

	my $ymd = toYMD($epoch);
	my ($logType) = $filename =~ m|(.*)\.\d+\.gz$|is;
	#print STDERR "date: ". $ymd . ", path: " . $path . ", filename: " . $filename . ', logType: ' . $logType . "\n";

	my $localName = $ymd . '.' . $logType . '.gz';
	if (!logPresent($localName)) {
		#print STDERR "not present $filename, $localName\n";
		fetchLog($filename, $localName);

		# notify the caller that we had to fetch
		return 1; 
	}
}

sub toYMD {
	my $epoch = shift;

	return strftime('%Y-%m-%d', localtime($epoch));
}

sub logPresent {
	my $logFile = shift;

	return -e './logs/' . $logFile;
}

sub fetchLog {
	my ($filename, $localName) = @_;

	print STDERR "fetching " . $localName . "\n";
	my $command = 'scp ' . $SSH_HOST . ':' . $LOG_PATH . '/' . $LOG_FILE . ' ./logs/' . $localName;
	system($command);
}
