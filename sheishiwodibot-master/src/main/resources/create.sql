CREATE DATABASE if not exists `sheishiwodi_db` ;
use sheishiwodi_db;
CREATE TABLE if not exists `telegram_group` (
    `id` int NOT NULL AUTO_INCREMENT,
    `groupId` char(14) NOT NULL,
    `firstJoinTime` datetime DEFAULT CURRENT_TIMESTAMP,
    `joinTime` datetime DEFAULT NULL,
    `finishGame` int DEFAULT '0',
    `botAdmin` tinyint(1) DEFAULT '0',
    `joinFrequency` int DEFAULT '0',
    `kickOutFrequency` int DEFAULT '0',
    `MaxOfPeople` int DEFAULT '0',
    `language` varchar(8) DEFAULT 'en',
    `userName` varchar(24) DEFAULT NULL,
    `title` char(24) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `groupId` (`groupId`)
) ENGINE=InnoDB ;

CREATE TABLE if not exists `telegram_user` (
    `id` int NOT NULL AUTO_INCREMENT,
    `telegram_id` char(14) DEFAULT NULL,
    `firstName` varchar(24) DEFAULT NULL,
    `lastName` varchar(24) DEFAULT NULL,
    `userName` varchar(24) DEFAULT NULL,
    `joinGame` int DEFAULT '0',
    `completeGame` int DEFAULT '0',
    `exitGame` int DEFAULT '0',
    `word_people` int DEFAULT '0',
    `word_spy` int DEFAULT '0',
    `word_people_victory` int DEFAULT '0',
    `word_spy_victory` int DEFAULT '0',
    `fraction` int DEFAULT '0',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `language` varchar(8) DEFAULT 'en',
    PRIMARY KEY (`id`),
    UNIQUE KEY `telegram_id` (`telegram_id`)
) ENGINE=InnoDB ;
CREATE USER if not exists 'sheishiwodibot'@'localhost'  ;
set password for 'sheishiwodibot'@'localhost' = 'sheishiwodibot_password';
GRANT USAGE ON *.* TO `sheishiwodibot`@`localhost`;
GRANT SELECT, INSERT ON `sheishiwodi_db`.* TO `sheishiwodibot`@`localhost`;
GRANT UPDATE (firstJoinTime,joinTime,finishGame,botAdmin,joinFrequency,
             kickOutFrequency,MaxOfPeople,language,userName,title) ON `sheishiwodi_db`.`telegram_group` TO `sheishiwodibot`@`localhost`;
GRANT UPDATE (firstName,lastName,userName,joinGame,completeGame,exitGame,
              word_people,word_spy,word_people_victory,
                word_spy_victory,fraction,
language) ON `sheishiwodi_db`.`telegram_user` TO `sheishiwodibot`@`localhost` ;
