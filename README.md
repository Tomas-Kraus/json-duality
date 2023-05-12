# JSON-Relational Duality Trials and Errors

```
Usage:
    mvn clean verify -Pstartdb,stopdb
```

Profiles:
 - `startdb`: Start Oracle 23c database in docker container and initialize DB schema and data
 - `stopdb`: Stop Oracle 23c database in docker container

Those profiles may be used to start and stop the database container in `pre-integration-test` and `post-integration-test` phases.

`Main` class execution is bound to `integration-test` phase, so it will be started as part of `verify` goal.
