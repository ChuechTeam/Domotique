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

-- changeset dynamic:add_devices

CREATE TABLE DeviceType(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    attributes JSON NOT NULL
);

CREATE TABLE Device(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    typeId INT NOT NULL,
    roomId INT NOT NULL,
    attributes JSON NOT NULL,
    powered BOOL NOT NULL,
    energyConsumption FLOAT NOT NULL,

    constraint fk_device_type FOREIGN KEY (typeId) REFERENCES DeviceType(id) ON DELETE RESTRICT,
    constraint fk_device_room FOREIGN KEY (roomId) REFERENCES Room(id) ON DELETE RESTRICT,
    INDEX idx_device_type(typeId),
    INDEX idx_device_room(roomId)
);

-- rollback drop table `Device`;
-- rollback drop table `DeviceType`;

-- changeset dynamic:add_login_logs

CREATE TABLE LoginLog(
    id INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NOT NULL,
    time DATETIME NOT NULL,

    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE CASCADE,
    INDEX idx_login_user(userId),
    index idx_time(time)
);