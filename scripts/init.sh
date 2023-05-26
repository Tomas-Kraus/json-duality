# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

# Initialize database user
for f in "$SCRIPTS_ROOT"/dba/*.sql; do
    echo "  Running as DBA: $f"
    echo "exit" | "$ORACLE_HOME"/bin/sqlplus -s "SYS/r00t_p4ssw0rd@FREEPDB1 as sysdba" @"$f"
    echo
done

# Initialize database schema
for f in "$SCRIPTS_ROOT"/user/*.sql; do
    echo "  Running as user: $f"
    echo "exit" | "$ORACLE_HOME"/bin/sqlplus -s "test/Us3r_P4ssw0rd@FREEPDB1" @"$f"
    echo
done

# Install JDK
echo "  Installing JDK 19"
(cd /opt/oracle && tar xfz install/openjdk-19.0.2_linux-x64_bin.tar.gz)
export JAVA_HOME='/opt/oracle/jdk-19.0.2'
export PATH=${PATH}:${JAVA_HOME}/bin

# Install OCI
echo "  Installing OCI"
(cd /opt/oracle && unzip -q install/instantclient-basic-linux.x64-21.10.0.0.0dbru.zip)
export LD_LIBRARY_PATH=/opt/oracle/instantclient_21_10
# Install ords
echo "  Installing ORDS"
(cd /opt/oracle && \
    mkdir ords && \
    cd ords && \
    mkdir config && \
    unzip -q ../install/ords-23.1.3.137.1032.zip)
export ORDS_CONFIG='/opt/oracle/ords/config'
(cd /opt/oracle/ords && \
    echo 'r00t_p4ssw0rd' > /opt/oracle/ords/ords_passwords.txt && \
    echo 'Us3r_P4ssw0rd' >> /opt/oracle/ords/ords_passwords.txt && \
    echo 'Us3r_P4ssw0rd' >> /opt/oracle/ords/ords_passwords.txt && \
    bin/ords install \
             --db-hostname localhost --db-port 1521 --db-servicename FREEPDB1 \
             --admin-user SYS --proxy-user \
             --schema-tablespace SYSAUX --schema-temp-tablespace TEMP \
             --proxy-user-tablespace SYSAUX --proxy-user-temp-tablespace TEMP \
             --bequeath-connect --feature-sdw true \
             --password-stdin < /opt/oracle/ords/ords_passwords.txt && \
    rm -f /opt/oracle/ords/ords_passwords.txt)
(cd /opt/oracle/ords && \
    bin/ords config set mongo.enabled true && \
    bin/ords serve &)

# Initialize Mongo API specific schema
for f in "$SCRIPTS_ROOT"/user/mongo/*.sql; do
    echo "  Running as user: $f"
    echo "exit" | "$ORACLE_HOME"/bin/sqlplus -s "test/Us3r_P4ssw0rd@FREEPDB1" @"$f"
    echo
done
