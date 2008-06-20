@echo off

set HSQLDB_LIB_DIR="C:/Documents and Settings/tom/jbpm/repository/hsqldb/1.8.0.2/lib"
set HSQLDB_DB_DIR=C:/Temp/test

start "DB" java -cp %HSQLDB_LIB_DIR%/hsqldb.jar org.hsqldb.Server -port 1701 -database %HSQLDB_DB_DIR%/db
