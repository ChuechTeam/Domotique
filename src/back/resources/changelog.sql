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

-- changeset dynamic:add_device_user_and_category
ALTER TABLE Device
    ADD userId INT NULL AFTER roomId,
    ADD category TINYINT NOT NULL DEFAULT 0 AFTER energyConsumption,
    ADD INDEX idx_device_user(userId),
    ADD CONSTRAINT fk_device_user FOREIGN KEY (userId) REFERENCES User(id) ON DELETE SET NULL;

-- rollback ALTER TABLE Device
-- rollback     DROP userId,
-- rollback     DROP category,
-- rollback     DROP INDEX idx_device_user,
-- rollback     DROP FOREIGN KEY fk_device_user;

-- changeset dynamic:fix_device_room_nullable

ALTER TABLE Device
    DROP FOREIGN KEY fk_device_room,
    DROP INDEX idx_device_room;

ALTER TABLE Device
    CHANGE roomId roomId INT NULL,
    ADD CONSTRAINT fk_device_room FOREIGN KEY (roomId) REFERENCES Room(id) ON DELETE SET NULL,
    ADD INDEX idx_device_room(roomId);

-- rollback empty

-- changeset dynamic:fix_desc_nullable_and_categories

ALTER TABLE Device
    CHANGE description description TEXT NULL,
    DROP category;

ALTER TABLE DeviceType
    ADD category TINYINT NOT NULL DEFAULT 0 AFTER name,
    ADD INDEX idx_device_type_category(category);

-- rollback ALTER TABLE Device
-- rollback     CHANGE description description TEXT NOT NULL,
-- rollback     ADD category TINYINT NOT NULL DEFAULT 0 AFTER energyConsumption;
--
-- rollback ALTER TABLE DeviceType
-- rollback     DROP category,
-- rollback     DROP INDEX idx_device_type_category;

-- changeset dynamic:addDeletionRequestedById

ALTER TABLE Device
    ADD deletionRequestedById INT NULL AFTER energyConsumption,
    ADD CONSTRAINT fk_device_deletion_requested_by FOREIGN KEY (deletionRequestedById) REFERENCES User(id) ON DELETE SET NULL;

-- rollback ALTER TABLE Device
-- rollback     DROP deletionRequestedById,
-- rollback     DROP FOREIGN KEY fk_device_deletion_requested_by;

-- changeset dynamic:add_action_log

CREATE TABLE ActionLog(
    id INT PRIMARY KEY AUTO_INCREMENT,
    userId INT NULL,
    targetId INT NOT NULL,
    targetType TINYINT NOT NULL,
    operation TINYINT NOT NULL,
    flags BIGINT NOT NULL,
    time DATETIME NOT NULL,

    FOREIGN KEY (userId) REFERENCES User(id) ON DELETE SET NULL,
    INDEX idx_actionlog_user(userId),
    INDEX idx_actionlog_target(targetId),
    INDEX idx_actionlog_time(time)
);

-- rollback drop table `ActionLog`;

-- changeset dynamic:add_consumption_to_powerlog

ALTER TABLE PowerLog
    ADD energyConsumption DOUBLE NOT NULL DEFAULT 0 AFTER status;

UPDATE PowerLog p
JOIN Device d on d.id = p.deviceId
SET p.energyConsumption = d.energyConsumption
WHERE p.status = 'POWER_ON' OR p.status = 'POWER_OFF';

-- rollback ALTER TABLE PowerLog
-- rollback     DROP energyConsumption;

-- changeset dynamic:add_invite_codes

CREATE TABLE InviteCode(
    id VARCHAR(16) PRIMARY KEY NOT NULL,
    usagesLeft INT NOT NULL CHECK ( usagesLeft > 0 ),
    role TINYINT NOT NULL, -- Role enum
    creatorId INT NULL,
    createdAt DATETIME NOT NULL,

    CONSTRAINT fk_invite_code_creator FOREIGN KEY (creatorId) REFERENCES User(id) ON DELETE SET NULL
)

-- rollback drop table `InviteCode`;