--Julie De Lorenzo jed76@pitt.edu
--Don Virostek


drop table profile cascade constraints;
create table profile (
	userID number(10) not null,
	name varchar2(15) not null,
	email varchar2(15) not null,
	password varchar2(10) not null,
	date_of_birth date,
	picture_URL varchar2(100),
	aboutme varchar2(250),
	lastlogin date,
	constraint pk_profile primary key(userID) initially deferred deferrable
);

drop table friends cascade constraints;
create table friends (
	userID1 number(10) not null,
	userID2 number(10) not null,
	JDate date,
	message varchar2(100),
	constraint pk_friends primary key(userID1, userID2) initially deferred deferrable,
	constraint fk_friends_user1 foreign key (userID1) references profile(userID) initially deferred deferrable,
	constraint fk_friends_user2 foreign key (userID2) references profile(userID) initially deferred deferrable
);

drop table pendingfriends cascade constraints;
create table pendingfriends (
	fromID number(10) not null,
	toID number(10) not null,
	message varchar2(100),
	constraint pk_pendingfriends primary key(fromID, toID) initially deferred deferrable,
	constraint fk_pendingfriendsfromID foreign key (fromID) references profile(userID) initially deferred deferrable,
	constraint fk_pendingfriendstoID foreign key (toID) references profile(userID) initially deferred deferrable
 );


drop table messages cascade constraints;
create table messages (
	msgID number(5) not null,
	fromID number(10),
	message varchar2(250),
	ToUserID number(10) default null,
	ToGroupID number(10) default null,
	dateSent date,
	constraint pk_messages primary key(msgID) initially deferred deferrable,
	constraint fk_messagesfromID foreign key (fromID) references profile(userID) initially deferred deferrable,
	constraint fk_messagestoUserID foreign key (ToUserID) references profile(userID) initially deferred deferrable,
	constraint fk_messagesToGroupID foreign key (ToGroupID) references groups(gID) initially deferred deferrable
);

drop table messageRecipient cascade constraints;
create table messageRecipient (
	userID number(10) not null,
	msgID number(5) not null,
	constraint pk_messageRecipient primary key (msgID, userID) initially deferred deferrable,
	constraint fk_messageRecipient_msgID foreign key (msgID) references messages (msgID) initially deferred deferrable,
	constraint fk_messageRecipient_userID foreign key (userID) references profile (userID) initially deferred deferrable
);

drop table groups cascade constraints;
create table groups (
	gID number(5) not null,
	name varchar2(15),
	description varchar2(250),
	constraint pk_groups primary key (gID) initially deferred deferrable
);

drop table groupMembership cascade constraints;
create table groupMembership(
	gID number(5) not null,
	userID number(10) not null,
	constraint pk_GroupMembership primary key (gID, userID) initially deferred deferrable, 
	constraint fk_GroupMembership_gID foreign key (gID) references groups (gID) initially deferred deferrable,
	constraint fk_GroupMembership_userID foreign key (userID) references profile (userID) initially deferred deferrable
);

create or replace trigger limit_groups
	before insert or update on groupMembership
	for each row
	declare
		num_groups number;
	begin
	   select count(gID) into num_groups from groupMembership where userID = :new.userID;
	   if num_groups >= 10
	   then
	   raise_application_error(-20101, 'A user can be a member of no more than 10 groups.');
  	   end if;
	end;
/
show errors;

create or replace trigger drop_acct
	after delete on profile
	for each row
	begin
		delete from groupMembership where userID = :old.userID;

		update messages m
			set m.ToGroupID = null 
			where not exists (select * from groupMembership where gID = m.ToGroupID and userID !=:old.userID);
		update messages
			set fromID = null
			where fromID =:old.userID;
		update messages
			set ToUserID = null
			where ToUserID = :old.userID;

		delete from messageRecipient
			where userID = :old.userID;
		delete from friends
			where userID1 = :old.userID or userID2 = :old.userID;
		delete from pendingfriends
			where fromID = :old.userID or toID = :old.userID;
		delete from messages
			where fromID =:old.userID and ToUserID is null and ToGroupID is null;
		delete from messages
			where ToUserID =:old.userID and fromID is null and ToGroupID is null;

	end;
/
show errors;

create or replace trigger send_message
	after insert on messages
	for each row
	begin
		if :new.ToUserId is not null
		then
			INSERT INTO MessageRecipient VALUES(:new.ToUserID, :new.msgID);
		elsif :new.ToGroupID is not null
		then
			FOR gUserId IN (select userid from GroupMembership where gID = :new.ToGroupID)
			LOOP
				INSERT INTO MessageRecipient VALUES(gUserId.userid, :new.msgID);
			END LOOP;
		else
			raise_application_error(-20101, 'Either a ToGroupID or ToUserID must be specified!');
		end if;
	end;
/
show errors;
		

INSERT INTO profile VALUES(1, 'Shenoda', 'shg@pitt.edu', 'shpwd', '13-OCT-1977', '/afs/pitt.edu/home/s/g/shg18/public/photo.jpg', 'CS 1555 TA', '11-NOV-2012');
INSERT INTO profile VALUES(2, 'Lory', 'lra@pitt.edu', 'lpwd', '08-MAR-1986', NULL, 'Member of ADMT Lab', '10-NOV-2012');
INSERT INTO profile VALUES(3, 'Peter', 'pdj@pitt.edu', 'ppwd', '09-JAN-1984', 'http://www.cs.pitt.edu/~peter', 'Graduate Student in CS dept.', '10-NOV-2012');
INSERT INTO profile VALUES(4,'Alexandrie', 'alx@pitt.edu', 'apwd', '21-AUG-1975', NULL, 'Architecture researcher', '11-NOV-2012');
INSERT INTO profile VALUES(5, 'Panickos', 'pnk@pitt.edu', 'kpwd', '08-SEP-1989', NULL, 'ADMT Lab researcher', '08-NOV-2012');
INSERT INTO profile VALUES(6, 'Socratis', 'soc@pitt.edu', 'spwd', '17-MAY-1981', NULL, 'TA in CS dept', '09-NOV-2012');
INSERT INTO profile VALUES(7, 'Yaw', 'yaw@pitt.edu', 'ypwd', '27-FEB-1987', NULL, 'Staff at CS dept', '07-NOV-2012');

INSERT INTO friends VALUES(1,2, '06-JAN-2008', 'Hey, it is me  Shenoda!' );
INSERT INTO friends VALUES(1,5, '15-JAN-2011', 'Hey, it is me  Shenoda!');
INSERT INTO friends VALUES(2,3,'23-AUG-2007', 'Hey, it is me  Lory!');
INSERT INTO friends VALUES(2,4,'17-FEB-2008', 'Hey, it is me  Lory!');
INSERT INTO friends VALUES(3,4,'16-SEP-2010', 'Hey, it is me  Peter!');
INSERT INTO friends VALUES(4,6,'06-OCT-2010', 'Hey, it is me  Alexandrie!');
INSERT INTO friends VALUES(6,7,'13-SEP-2012', 'Hey, it is me  Socratis!');

INSERT INTO pendingfriends VALUES(7,4,'Hey, it is me Yaw');
INSERT INTO pendingfriends VALUES(5,2,'Hey, it is me Panickos');
INSERT INTO pendingfriends VALUES(2,6,'Hey, it is me Lory');

INSERT INTO Groups VALUES(1, 'Grads at CS', 'list of all graduate students');
INSERT INTO Groups VALUES(2, 'DB Group', 'member of the ADMT Lab.');

INSERT INTO GroupMembership VALUES(1,1);
INSERT INTO GroupMembership VALUES(1,2);
INSERT INTO GroupMembership VALUES(1,3);
INSERT INTO GroupMembership VALUES(1,4);
INSERT INTO GroupMembership VALUES(1,5);
INSERT INTO GroupMembership VALUES(1,6);
INSERT INTO GroupMembership VALUES(1,7);
INSERT INTO GroupMembership VALUES(2,1);
INSERT INTO GroupMembership VALUES(2,2);
INSERT INTO GroupMembership VALUES(2,5);

INSERT INTO messages VALUES(1, 1, 'are we meeting tomorrow for the project?', 2, NULL, '09-NOV-2012');
INSERT INTO messages VALUES(2, 1, 'Peter''s pub tomorrow?', 5, NULL, '07-NOV-2012');
INSERT INTO messages VALUES(3, 2, 'Please join our DB Group forum tomorrow', NULL, 1, '06-NOV-2012');
INSERT INTO messages VALUES(4, 5, 'Here is the paper I will present tomorrow', NULL, 2, '04-NOV-2012');

--INSERT INTO MessageRecipient VALUES(2,1);
--INSERT INTO MessageRecipient VALUES(5,2);
--INSERT INTO MessageRecipient VALUES(1,3);
--INSERT INTO MessageRecipient VALUES(2,3);
--INSERT INTO MessageRecipient VALUES(3,3);
--INSERT INTO MessageRecipient VALUES(4,3);
--INSERT INTO MessageRecipient VALUES(5,3);
--INSERT INTO MessageRecipient VALUES(6,3);
--INSERT INTO MessageRecipient VALUES(7,3);
--INSERT INTO MessageRecipient VALUES(1,4);
--INSERT INTO MessageRecipient VALUES(2,4);
--INSERT INTO MessageRecipient VALUES(5,4);


prompt finished creating tables and inserting data;
purge recyclebin;

