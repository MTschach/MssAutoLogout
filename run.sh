#!/bin/bash

APP=build/libs/AutoLogoutClient.jar
LIBBASE=build/output/libs

CLASSPATH="$APP"

for f in $LIBBASE/* ; do
      CLASSPATH="$CLASSPATH:$f"
done

CLASSPATH="$APP:$LIBBASE/*"

echo "java -Dlog4j.configurationFile=log4j2.properties -Dfile.encoding=UTF-8 -cp $CLASSPATH de.mss.autologout.client.AutoLogoutClient -f autologout.client.conf"
java -Dlog4j.configurationFile=log4j2.properties -Dfile.encoding=UTF-8 -cp $CLASSPATH de.mss.autologout.client.AutoLogoutClient -f autologout.client.conf

