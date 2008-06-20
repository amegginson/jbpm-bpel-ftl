set JBPM_SRC_HOME=c:\wsjbpm\jbpm.3_HEAD
set INSTALL_DIR=c:\software
set SUITE_DIR=%INSTALL_DIR%\jbpm-jpdl-3.2.Beta1

cmd /C ant -f %JBPM_SRC_HOME%/build/build.xml clean
cmd /C ant -f %JBPM_SRC_HOME%/jpdl/dist/build.xml
rmdir /S /Q %SUITE_DIR%
unzip -q %JBPM_SRC_HOME%/jpdl/dist/target/jbpm-jpdl-suite.zip -d %INSTALL_DIR%
cd %SUITE_DIR%\designer
cmd /C ant -f %SUITE_DIR%\designer\build.xml
start /D/D%SUITE_DIR%\server "JBPM SERVER" %SUITE_DIR%\server\start.bat
cd /D%SUITE_DIR%\designer 
%SUITE_DIR%\designer\designer.bat
%SUITE_DIR%\server\stop.bat
