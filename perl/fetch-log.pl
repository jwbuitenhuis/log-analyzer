#!/usr/bin/perl -w

use strict;
use POSIX 'strftime';
use Cwd;

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
		fetchLog($filename, $localName);

		# notify the caller that we had to fetch
		return 1; 
	} else {
		print STDERR "log $filename already present locally\n";
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

    my $dir = getcwd;

	my $command = 'scp ' . $ENV{SSH_HOST} . ':' . $ENV{LOG_PATH} . '/' . $filename . ' ' . $dir . '/logs/' . $localName;
	system($command);
}
