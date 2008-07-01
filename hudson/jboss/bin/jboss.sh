#!/bin/sh

PROGNAME=`basename $0`
DIRNAME=`dirname $0`
JBOSS_HOME="$1"
BINDADDR="$3"
CMD="$2"

export JBOSS_HOME

#
# Helper to complain.
#
warn() {
   echo "$PROGNAME: $*"
}

if [ ! -f "$JBOSS_HOME/bin/run.sh" ]; then
   warn "Cannot find: $JBOSS_HOME/bin/run.sh"
   exit 1
fi

case "$CMD" in
start)
    # This version of run.sh obtains the pid of the JVM and saves it as jboss.pid
    # It relies on bash specific features
    # Do you want to hide jboss output?
    /bin/bash $DIRNAME/runjboss.sh -b $BINDADDR &
    ;;
stop)
    pidfile="$JBOSS_HOME/bin/jboss.pid"
    if [ -f "$pidfile" ]; then
       pid=`cat "$pidfile"`
       echo "kill pid: $pid"
       kill $pid
       if [ "$?" -eq 0 ]; then
         # process exists, wait for it to die, and force if not
         sleep 20
         kill -9 $pid &> /dev/null
       fi
       rm "$pidfile"
    else
       warn "No pid found, using shutdown"
       $JBOSS_HOME/bin/shutdown.sh -S > /dev/null &
    fi
    ;;
restart)
    $0 stop
    $0 start
    ;;
*)
    echo "usage: $0 jboss_instance (start|stop|restart|help)"
esac
