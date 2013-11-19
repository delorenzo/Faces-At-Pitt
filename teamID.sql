--Julie De Lorenzo jed76@pitt.edu
--Don Virostek

drop table profile cascade constraints;
create table profile (
	userID varchar2(10) not null,
	name varchar2(15) not null,
	email varchar2(15) not null,
	password varchar2(10) not null,
	date_of_birth date,
	picture_URL varchar2(20),
	aboutme varchar2(250),
	lastlogin date,
	constraint pk_profile primary key(userID)
);

drop table friends cascade constraints;
create table friends (
	userID1 varchar2(10) not null,
	userID2 varchar2(10) not null,
	JDate date;
	message varchar2(100),

	constraint pk_friends primary key(userID1, userID2),
	constraint fk_friendsuser1 foreign key (userID1) references profile(userID),
	constraint fk_friendsuser2 foreign key (userID2) references profile(userID)
);

drop table pendingfriends cascade constraints;
create table pendingfriends (
	fromID varchar2(10) not null,
	toID varchar2(10) not null,
	message varchar2(100),

	constraint pk_pendingfriends primary key(fromID, toID),
	constraint fk_pendingfriendsfromID foreign key (fromID) references profile(userID),
	constraint fk_pendingfriendstoID foreign key (toID) references profile(userID)
 );


drop table messages cascade constraints;
create table messages (
	msgID varchar2(5) not null,
	fromID varchar2(10) not null,
	message varchar2(250),
	ToUserID varchar2(10) default null,
	ToGroupID varchar2(10) default null,
	date date,

	constraint pk_messages primary key(msgID),
	constraint fk_messagesfromID foreign key(fromID) references profile(userID)
);

drop table messageRecipient cascade constraints;
create table messageRecipient (
	msgID varchar2(5) not null,
	userID varchar2(10) not null,

	constraint pk_messageRecipient primary key (msgID, userID),
	constraint fk_messageRecipientmsgID foreign key (msgID) references messages (msgID),
	constraint fk_messageRecipientuserID foreign key (userID) references profile (userID)

);

drop table groups cascade constraints;
create table groups (
	gID varchar2(5) not null,
	name varchar2(15),
	description varchar2(250),

	primary key pk_groups primary key (gID)
);

drop table groupMembership cascade constraints;
create table groupMembership(
	gID varchar2(5) not null,
	userID varchar2(10) not null,

	constraint pk_GroupMembership primary key (gID, userID),
	constraint fk_GroupMembership_gID foreign key (gID) references groups (gID),
	constraint fk_GroupMembership_userID foreign key (userID) references profile (userID)

);