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
