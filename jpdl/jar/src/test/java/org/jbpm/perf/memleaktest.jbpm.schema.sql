create table JBPM_ID_GROUP(ID_ BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,CLASS_ CHAR(1) NOT NULL,NAME_ VARCHAR(255),TYPE_ VARCHAR(255),PARENT_ BIGINT,CONSTRAINT FK_ID_GRP_PARENT FOREIGN KEY(PARENT_) REFERENCES JBPM_ID_GROUP(ID_))
create table JBPM_ID_MEMBERSHIP(ID_ BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,CLASS_ CHAR(1) NOT NULL,NAME_ VARCHAR(255),ROLE_ VARCHAR(255),USER_ BIGINT,GROUP_ BIGINT,CONSTRAINT FK_ID_MEMSHIP_GRP FOREIGN KEY(GROUP_) REFERENCES JBPM_ID_GROUP(ID_))
create table JBPM_ID_PERMISSIONS(ENTITY_ BIGINT NOT NULL,CLASS_ VARCHAR(255),NAME_ VARCHAR(255),ACTION_ VARCHAR(255))
create table JBPM_ID_USER(ID_ BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,CLASS_ CHAR(1) NOT NULL,NAME_ VARCHAR(255),EMAIL_ VARCHAR(255),PASSWORD_ VARCHAR(255))
create table JBPM_ACTION (ID_ bigint generated by default as identity (start with 1), class char(1) not null, NAME_ varchar(255), ISPROPAGATIONALLOWED_ bit, ACTIONEXPRESSION_ varchar(255), ISASYNC_ bit, REFERENCEDACTION_ bigint, ACTIONDELEGATION_ bigint, EVENT_ bigint, PROCESSDEFINITION_ bigint, TIMERNAME_ varchar(255), DUEDATE_ varchar(255), REPEAT_ varchar(255), TRANSITIONNAME_ varchar(255), TIMERACTION_ bigint, EXPRESSION_ varchar(4000), EVENTINDEX_ integer, EXCEPTIONHANDLER_ bigint, EXCEPTIONHANDLERINDEX_ integer, primary key (ID_))
create table JBPM_BYTEARRAY (ID_ bigint generated by default as identity (start with 1), NAME_ varchar(255), FILEDEFINITION_ bigint, primary key (ID_))
create table JBPM_BYTEBLOCK (PROCESSFILE_ bigint not null, BYTES_ varbinary(1024), INDEX_ integer not null, primary key (PROCESSFILE_, INDEX_))
create table JBPM_COMMENT (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, ACTORID_ varchar(255), TIME_ timestamp, MESSAGE_ varchar(4000), TOKEN_ bigint, TASKINSTANCE_ bigint, TOKENINDEX_ integer, TASKINSTANCEINDEX_ integer, primary key (ID_))
create table JBPM_DECISIONCONDITIONS (DECISION_ bigint not null, TRANSITIONNAME_ varchar(255), EXPRESSION_ varchar(255), INDEX_ integer not null, primary key (DECISION_, INDEX_))
create table JBPM_DELEGATION (ID_ bigint generated by default as identity (start with 1), CLASSNAME_ varchar(4000), CONFIGURATION_ varchar(4000), CONFIGTYPE_ varchar(255), PROCESSDEFINITION_ bigint, primary key (ID_))
create table JBPM_EVENT (ID_ bigint generated by default as identity (start with 1), EVENTTYPE_ varchar(255), TYPE_ char(1), GRAPHELEMENT_ bigint, PROCESSDEFINITION_ bigint, NODE_ bigint, TRANSITION_ bigint, TASK_ bigint, primary key (ID_))
create table JBPM_EXCEPTIONHANDLER (ID_ bigint generated by default as identity (start with 1), EXCEPTIONCLASSNAME_ varchar(4000), TYPE_ char(1), GRAPHELEMENT_ bigint, PROCESSDEFINITION_ bigint, GRAPHELEMENTINDEX_ integer, NODE_ bigint, TRANSITION_ bigint, TASK_ bigint, primary key (ID_))
create table JBPM_JOB (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, VERSION_ integer not null, DUEDATE_ timestamp, PROCESSINSTANCE_ bigint, TOKEN_ bigint, TASKINSTANCE_ bigint, ISSUSPENDED_ bit, ISEXCLUSIVE_ bit, LOCKOWNER_ varchar(255), LOCKTIME_ timestamp, EXCEPTION_ varchar(4000), RETRIES_ integer, NAME_ varchar(255), REPEAT_ varchar(255), TRANSITIONNAME_ varchar(255), ACTION_ bigint, GRAPHELEMENTTYPE_ varchar(255), GRAPHELEMENT_ bigint, NODE_ bigint, primary key (ID_))
create table JBPM_LOG (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, INDEX_ integer, DATE_ timestamp, TOKEN_ bigint, PARENT_ bigint, MESSAGE_ varchar(4000), EXCEPTION_ varchar(4000), ACTION_ bigint, NODE_ bigint, ENTER_ timestamp, LEAVE_ timestamp, DURATION_ bigint, NEWLONGVALUE_ bigint, TRANSITION_ bigint, CHILD_ bigint, SOURCENODE_ bigint, DESTINATIONNODE_ bigint, VARIABLEINSTANCE_ bigint, OLDBYTEARRAY_ bigint, NEWBYTEARRAY_ bigint, OLDDATEVALUE_ timestamp, NEWDATEVALUE_ timestamp, OLDDOUBLEVALUE_ double, NEWDOUBLEVALUE_ double, OLDLONGIDCLASS_ varchar(255), OLDLONGIDVALUE_ bigint, NEWLONGIDCLASS_ varchar(255), NEWLONGIDVALUE_ bigint, OLDSTRINGIDCLASS_ varchar(255), OLDSTRINGIDVALUE_ varchar(255), NEWSTRINGIDCLASS_ varchar(255), NEWSTRINGIDVALUE_ varchar(255), OLDLONGVALUE_ bigint, OLDSTRINGVALUE_ varchar(4000), NEWSTRINGVALUE_ varchar(4000), TASKINSTANCE_ bigint, TASKACTORID_ varchar(255), TASKOLDACTORID_ varchar(255), SWIMLANEINSTANCE_ bigint, primary key (ID_))
create table JBPM_MODULEDEFINITION (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, NAME_ varchar(4000), PROCESSDEFINITION_ bigint, STARTTASK_ bigint, primary key (ID_))
create table JBPM_MODULEINSTANCE (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, VERSION_ integer not null, PROCESSINSTANCE_ bigint, TASKMGMTDEFINITION_ bigint, NAME_ varchar(255), primary key (ID_))
create table JBPM_NODE (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, NAME_ varchar(255), DESCRIPTION_ varchar(4000), PROCESSDEFINITION_ bigint, ISASYNC_ bit, ISASYNCEXCL_ bit, ACTION_ bigint, SUPERSTATE_ bigint, SUBPROCNAME_ varchar(255), SUBPROCESSDEFINITION_ bigint, DECISIONEXPRESSION_ varchar(255), DECISIONDELEGATION bigint, SCRIPT_ bigint, SIGNAL_ integer, CREATETASKS_ bit, ENDTASKS_ bit, NODECOLLECTIONINDEX_ integer, primary key (ID_))
create table JBPM_POOLEDACTOR (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, ACTORID_ varchar(255), SWIMLANEINSTANCE_ bigint, primary key (ID_))
create table JBPM_PROCESSDEFINITION (ID_ bigint generated by default as identity (start with 1), NAME_ varchar(255), DESCRIPTION_ varchar(4000), VERSION_ integer, ISTERMINATIONIMPLICIT_ bit, STARTSTATE_ bigint, primary key (ID_))
create table JBPM_PROCESSINSTANCE (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, START_ timestamp, END_ timestamp, ISSUSPENDED_ bit, PROCESSDEFINITION_ bigint, ROOTTOKEN_ bigint, SUPERPROCESSTOKEN_ bigint, primary key (ID_))
create table JBPM_RUNTIMEACTION (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, EVENTTYPE_ varchar(255), TYPE_ char(1), GRAPHELEMENT_ bigint, PROCESSINSTANCE_ bigint, ACTION_ bigint, PROCESSINSTANCEINDEX_ integer, primary key (ID_))
create table JBPM_SWIMLANE (ID_ bigint generated by default as identity (start with 1), NAME_ varchar(255), ACTORIDEXPRESSION_ varchar(255), POOLEDACTORSEXPRESSION_ varchar(255), ASSIGNMENTDELEGATION_ bigint, TASKMGMTDEFINITION_ bigint, primary key (ID_))
create table JBPM_SWIMLANEINSTANCE (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, NAME_ varchar(255), ACTORID_ varchar(255), SWIMLANE_ bigint, TASKMGMTINSTANCE_ bigint, primary key (ID_))
create table JBPM_TASK (ID_ bigint generated by default as identity (start with 1), NAME_ varchar(255), DESCRIPTION_ varchar(4000), PROCESSDEFINITION_ bigint, ISBLOCKING_ bit, ISSIGNALLING_ bit, CONDITION_ varchar(255), DUEDATE_ varchar(255), PRIORITY_ integer, ACTORIDEXPRESSION_ varchar(255), POOLEDACTORSEXPRESSION_ varchar(255), TASKMGMTDEFINITION_ bigint, TASKNODE_ bigint, STARTSTATE_ bigint, ASSIGNMENTDELEGATION_ bigint, SWIMLANE_ bigint, TASKCONTROLLER_ bigint, primary key (ID_))
create table JBPM_TASKACTORPOOL (TASKINSTANCE_ bigint not null, POOLEDACTOR_ bigint not null, primary key (TASKINSTANCE_, POOLEDACTOR_))
create table JBPM_TASKCONTROLLER (ID_ bigint generated by default as identity (start with 1), TASKCONTROLLERDELEGATION_ bigint, primary key (ID_))
create table JBPM_TASKINSTANCE (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, VERSION_ integer not null, NAME_ varchar(255), DESCRIPTION_ varchar(4000), ACTORID_ varchar(255), CREATE_ timestamp, START_ timestamp, END_ timestamp, DUEDATE_ timestamp, PRIORITY_ integer, ISCANCELLED_ bit, ISSUSPENDED_ bit, ISOPEN_ bit, ISSIGNALLING_ bit, ISBLOCKING_ bit, TASK_ bigint, TOKEN_ bigint, SWIMLANINSTANCE_ bigint, TASKMGMTINSTANCE_ bigint, primary key (ID_))
create table JBPM_TOKEN (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, NAME_ varchar(255), START_ timestamp, END_ timestamp, NODEENTER_ timestamp, NEXTLOGINDEX_ integer, ISABLETOREACTIVATEPARENT_ bit, ISTERMINATIONIMPLICIT_ bit, ISSUSPENDED_ bit, LOCK_ varchar(255), NODE_ bigint, PROCESSINSTANCE_ bigint, PARENT_ bigint, SUBPROCESSINSTANCE_ bigint, primary key (ID_))
create table JBPM_TOKENVARIABLEMAP (ID_ bigint generated by default as identity (start with 1), VERSION_ integer not null, TOKEN_ bigint, CONTEXTINSTANCE_ bigint, primary key (ID_))
create table JBPM_TRANSITION (ID_ bigint generated by default as identity (start with 1), NAME_ varchar(255), DESCRIPTION_ varchar(4000), PROCESSDEFINITION_ bigint, FROM_ bigint, TO_ bigint, CONDITION_ varchar(255), FROMINDEX_ integer, primary key (ID_))
create table JBPM_VARIABLEACCESS (ID_ bigint generated by default as identity (start with 1), VARIABLENAME_ varchar(255), ACCESS_ varchar(255), MAPPEDNAME_ varchar(255), PROCESSSTATE_ bigint, TASKCONTROLLER_ bigint, INDEX_ integer, SCRIPT_ bigint, primary key (ID_))
create table JBPM_VARIABLEINSTANCE (ID_ bigint generated by default as identity (start with 1), CLASS_ char(1) not null, VERSION_ integer not null, NAME_ varchar(255), CONVERTER_ char(1), TOKEN_ bigint, TOKENVARIABLEMAP_ bigint, PROCESSINSTANCE_ bigint, BYTEARRAYVALUE_ bigint, DATEVALUE_ timestamp, DOUBLEVALUE_ double, LONGIDCLASS_ varchar(255), LONGVALUE_ bigint, STRINGIDCLASS_ varchar(255), STRINGVALUE_ varchar(255), TASKINSTANCE_ bigint, primary key (ID_))
