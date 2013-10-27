# FeedReader
Application for reading RSS feeds.

## Requirements 
In order to run the application you will need PostgreSQL installed.

## Database Setup
The application is designed to use PostgreSQL so that will need to be installed. The scripts were tested against version 9.2 so your mileage may vary if you use a different version.

1. In Terminal, navigate to the root of the project and run the following 
    
    plsql -h localhost
    
2. Execute the create script
     
    \i src/main/sql/create.sql
     
You should now have a database named "feedreader" installed in PostgreSQL. 

## Application Setup
The application loads its configuration from properties files loaded at runtime. To override any properties, add a properties file at `src/main/resources/feedreader/config-user.properties`. In the config-user.properties file you must specify at least the `dataSource.user`, which is the user used to connect to the PostgreSQL instance. For other properties that can be overridden, see the `config-default.properties` file.
