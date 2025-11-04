CREATE DATABASE game_zone;
USE game_zone;


CREATE TABLE players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    credits INT NOT NULL DEFAULT 500,
)

INSERT INTO players(username, password)
VALUES('test', 'pass');