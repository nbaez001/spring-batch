DROP TABLE IF EXISTS student;

CREATE TABLE student (
  id bigint NOT NULL AUTO_INCREMENT,
  first_name varchar(45) DEFAULT NULL,
  last_name varchar(45) DEFAULT NULL,
  email varchar(45) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO student VALUES
(1,'John','Smith','john@gmail.com'),
(2,'Sachin','Dave','sachin@gmail.com'),
(3,'Peter','Mark','peter@gmail.com'),
(4,'Martin','Smith','martin@gmail.com'),
(5,'Raj','Patel','raj@gmail.com'),
(6,'Virat','Yadav','virat@gmail.com'),
(7,'Prabhas','Shirke','prabhas@gmail.com'),
(8,'Tina','Kapoor','tina@gmail.com'),
(9,'Mona','Sharma','mona@gmail.com'),
(10,'Rahul','Varma','rahul@gmail.com');