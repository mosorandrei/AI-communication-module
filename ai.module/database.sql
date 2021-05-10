CREATE TABLE ai_entity
(
	id integer NOT NULL PRIMARY KEY,
	title varchar(100) NOT NULL,
	content varchar(500) NOT NULL
)

CREATE TABLE ai_result_entity
(
	id integer NOT NULL PRIMARY KEY,
	title varchar(100) NOT NULL,
	content varchar(500) NOT NULL,
	result varchar(50)
)

INSERT INTO ai_result_entity (id, title, content, result) 
	VALUES (1, 'title1', 'content1', 'true');
	
SELECT * FROM ai_result_entity;