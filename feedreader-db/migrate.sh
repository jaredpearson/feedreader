#!/bin/bash

flyway_version=3.2.1
flyway_zip_filename="flyway-commandline-$flyway_version.zip"
flyway_cmdline_url="http://repo1.maven.org/maven2/org/flywaydb/flyway-commandline/$flyway_version/$flyway_zip_filename"
bin_dir="$PWD/bin"
bin_flyway_root="$bin_dir/flyway"
bin_flyway_zip="$bin_flyway_root/$flyway_zip_filename"
flyway_home="$bin_flyway_root/flyway-$flyway_version"
flyway_bin="$flyway_home/flyway"
db_dir=$PWD

[[ -d $bin_flyway_root ]] || mkdir -p $bin_flyway_root

if [ ! -f $flyway_bin ]; then
	echo "Flyway not found at $flyway_bin"
	echo "Installing"

	if [ ! -f $bin_flyway_zip ]; then
		cd $bin_flyway_root
		curl -O $flyway_cmdline_url
	else 
		echo "Using $bin_flyway_zip"
	fi

	unzip $bin_flyway_zip -d $bin_flyway_root
else 
	echo "Found Flyway at $flyway_bin"
fi

$flyway_home/flyway -url='jdbc:postgresql://192.168.52.13:5432/feedreader' -user=postgres -schemas=feedreader -password='rDVSPDaSXXg5J99' -locations="filesystem:$db_dir/sql" migrate