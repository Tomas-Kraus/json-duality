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

for f in "$SCRIPTS_ROOT"/dba/*.sql; do
    echo "  Running as DBA: $f"
    echo "exit" | "$ORACLE_HOME"/bin/sqlplus -s "SYS/r00t_p4ssw0rd@FREEPDB1 as sysdba" @"$f"
    echo
done

for f in "$SCRIPTS_ROOT"/user/*.sql; do
    echo "  Running as user: $f"
    echo "exit" | "$ORACLE_HOME"/bin/sqlplus -s "test/Us3r_P4ssw0rd@FREEPDB1" @"$f"
    echo
done
