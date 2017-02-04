# FeedReader
Application for reading RSS feeds.

## Requirements 
In order to run the application you will need PostgreSQL installed.

## Provisioning

You must have the following tools installed

* [Vagrant](https://www.vagrantup.com/)
* [Librarian](https://github.com/applicationsonline/librarian-chef)

1. `cd provisioning/chef'
2. `librarian-chef install`
3. `cd ../..`
4. `vagrant up`

There should now be a VM image containing the database at port `192.168.52.13`.

## Database Setup
The application is designed to use PostgreSQL which is installed during provisioning.

1. In Terminal, navigate to the feedreader-db project and run the following 
    
    sh migrate.sh
         
The migrations use Flyway so it will download and install if it's not already installed. After the migrations run you should now have a database named "feedreader" installed in PostgreSQL. 

## Message Queue Setup
The application uses Apache Qpid for processing asynchronous messages.

1. Create a virtual host named `feedreader`
2. Create a queue named `feed.request`

## Application Configuration
The application loads its configuration from properties files loaded at runtime. To override any properties, add a properties file at `src/main/resources/feedreader/config-user.properties`. In the config-user.properties file you must specify at least the `dataSource.user`, which is the user used to connect to the PostgreSQL instance. For other properties that can be overridden, see the `config-default.properties` file.

## REST Examples
Here are a few examples but see the tests within the feedreader-functional-test project for more.

### Viewing a stream
To request the stream of feeds, run the following cURL command, where "<session_id>" is the ID of your session.

    curl http://localhost:8080/services/v1/stream -H "Authorization:SID <session_id>"

### Adding a new feed
To request a new feed be added to your reader, run the following cURL command, where "<session_id>" is the ID of your session.

    curl http://localhost:8080/services/v1/feedSubscriptions -H "Authorization:SID <session_id>" -X POST -H "Content-Type: application/json" -d '{"url":"http://www.nasa.gov/rss/dyn/breaking_news.rss"}' 
