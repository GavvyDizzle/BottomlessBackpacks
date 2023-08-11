CREATE TABLE IF NOT EXISTS backpacks
(
    uuid BINARY(16) NOT NULL,
    pages INT DEFAULT 1,
    savePage BOOLEAN DEFAULT 0,
    items MEDIUMBLOB,
    PRIMARY KEY (uuid)
);