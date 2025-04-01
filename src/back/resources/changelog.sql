-- liquibase formatted sql
-- Guide:
-- https://docs.liquibase.com/concepts/changelogs/changeset.html
-- https://docs.liquibase.com/concepts/changelogs/sql-format.html
-- To update the database, multiple options:
--    -> gradle updateDatabase
--    -> ./liquibase
--
-- Want to rollback? Do one of these:
--    -> gradle updateDatabase --args="rollback-count 1"
--    -> ./liquibase rollbackCount 1
--
-- Want to NUKE the database? Do this!!
--   -> gradle updateDatabase --args="drop-all"
--   -> ./liquibase drop-all

-- changeset dynamic:init
CREATE TABLE User(
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(128) NOT NULL,
    emailConfirmationToken BIGINT NOT NULL,
    emailConfirmed BOOL NOT NULL,
    passHash VARCHAR(128) NOT NULL,
    firstName VARCHAR(128) NOT NULL,
    lastName VARCHAR(128) NOT NULL,
    gender TINYINT NOT NULL,
    role TINYINT NOT NULL,
    level TINYINT NOT NULL,
    points INT NOT NULL,

    UNIQUE INDEX idx_mail(email)
);

-- rollback drop table `User`;

-- changeset dynamic:add_rooms
CREATE TABLE Room(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    color INT NOT NULL,
    ownerId INT,

    FOREIGN KEY (ownerId) REFERENCES User(id) ON DELETE SET NULL,
    INDEX idx_room_owner(ownerId)
);

-- rollback drop table `Room`;

-- changeset Evan:add_power_logs
CREATE TABLE PowerLog(
    id INT PRIMARY KEY AUTO_INCREMENT,
    deviceId INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    time DATETIME NOT NULL,

    FOREIGN KEY (deviceId) REFERENCES Device(id) ON DELETE CASCADE,
    INDEX idx_powerlog_device (deviceId),
    INDEX idx_powerlog_time (time)
);

-- rollback drop table `PowerLog`;
