/*
 * Feedreader PostgreSQL install script
 *
 * See the README for installation instructions.
 */

CREATE SEQUENCE users_id_seq;
CREATE TABLE Users (
  id integer NOT NULL DEFAULT nextval('users_id_seq') PRIMARY KEY,
  email varchar(128) NOT NULL,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE users_id_seq OWNED BY Users.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE Users TO feedreader_app;


CREATE SEQUENCE usersessions_id_seq;
CREATE TABLE UserSessions (
  id integer NOT NULL DEFAULT nextval('usersessions_id_seq') PRIMARY KEY,
  userId integer NOT NULL REFERENCES Users(id),
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE usersessions_id_seq OWNED BY UserSessions.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE UserSessions TO feedreader_app;

CREATE SEQUENCE feeds_id_seq;
CREATE TABLE Feeds (
  id integer NOT NULL DEFAULT nextval('feeds_id_seq') PRIMARY KEY,
  url varchar(1024) NOT NULL,
  lastUpdated timestamp,
  title varchar(200),
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy integer NOT NULL REFERENCES Users(id)
);
ALTER SEQUENCE feeds_id_seq OWNED BY Feeds.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE Feeds TO feedreader_app;

CREATE SEQUENCE feeditems_id_seq;
CREATE TABLE FeedItems (
  id integer NOT NULL DEFAULT nextval('feeditems_id_seq') PRIMARY KEY,
  feedId integer NOT NULL REFERENCES Feeds(id),
  title varchar(1024),
  description text,
  link varchar(1024),
  pubDate timestamp,
  guid varchar(256),
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE feeditems_id_seq OWNED BY FeedItems.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE FeedItems TO feedreader_app;

CREATE SEQUENCE feedrequests_id_seq;
CREATE TABLE FeedRequests (
  id integer NOT NULL DEFAULT nextval('feedrequests_id_seq') PRIMARY KEY,
  url varchar(1024) NOT NULL,
  feedId integer REFERENCES Feeds(id),
  status varchar(10),
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  createdBy integer NOT NULL REFERENCES Users(id)
);
ALTER SEQUENCE feedrequests_id_seq OWNED BY FeedRequests.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE FeedRequests TO feedreader_app;

CREATE SEQUENCE userfeeditemcontexts_id_seq;
CREATE TABLE UserFeedItemContexts (
  id integer NOT NULL DEFAULT nextval('userfeeditemcontexts_id_seq') PRIMARY KEY,
  feedItemId integer NOT NULL REFERENCES FeedItems(id),
  owner integer NOT NULL REFERENCES Users(id),
  read boolean DEFAULT TRUE,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE userfeeditemcontexts_id_seq OWNED BY UserFeedItemContexts.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE UserFeedItemContexts TO feedreader_app;

CREATE SEQUENCE feedsubscriptions_id_seq;
CREATE TABLE FeedSubscriptions (
  id integer NOT NULL DEFAULT nextval('feedsubscriptions_id_seq') PRIMARY KEY,
  feedId integer REFERENCES Feeds(id) ON DELETE CASCADE,
  subscriber integer NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
  created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE feedsubscriptions_id_seq OWNED BY FeedSubscriptions.id;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE FeedSubscriptions TO feedreader_app;
