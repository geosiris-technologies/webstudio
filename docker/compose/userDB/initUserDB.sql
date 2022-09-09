/*create database geosirisuser; */
\c geosirisuser;
create type user_group as enum ('geosiris', 'user');
create table account(
    user_id serial primary key, 
    login varchar (255) unique not null,
    password varchar (255) not null,
    mail varchar (255),
    usr_grp user_group
);

INSERT INTO account (login, password, mail, usr_grp)
VALUES  ('admin', 'GP7HklAw/uG7JmtiAE/ApQ==', 'admin@example.com', 'geosiris');
INSERT INTO account (login, password, mail, usr_grp)
VALUES  ('user', 'nBh4QQg7tS9wtS8WVrTtMQ==', 'admin@example.com', 'user');
/* admin password is nimda */
/* user password is resu */

/*openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -sha256 -days 365*/