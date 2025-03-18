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

-- liquibase formatted sql

-- changeset dynamic:init
CREATE TABLE user(
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    gender TINYINT NOT NULL,
    pass_hash VARCHAR(255) NOT NULL,

    UNIQUE INDEX idx_mail(email)
);

-- rollback drop table user;