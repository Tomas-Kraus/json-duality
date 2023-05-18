# JSON-Relational Duality Trials and Errors

## EclipseLink and JPA-RS example

```
Usage:
    mvn clean verify -Pjersey,startdb,stopdb
```

Profiles:
 - `jersey`: Start HTTP service (Jerzey + Grizzly)
 - `startdb`: Start Oracle 23c database in docker container and initialize DB schema and data
 - `stopdb`: Stop Oracle 23c database in docker container

HTTP service profile starts HTTP server during `integration-test` phase.
Database related profiles may be used to start and stop the database container in `pre-integration-test` and `post-integration-test` phases.
So all profiles will be executed as part of `verify` goal.

Following command may be used to call REST API `GET` request with JSON response using `curl`:
- `curl -H "Content-Type: application/json" <url>`

Looks like JPA-RS does not contain service for listing all stored entity instances, e.g. `listAll`. But named query may be used for this purpose.
- http://localhost:8080/latest/jpars_example/query/ListAllTypes
- http://localhost:8080/latest/jpars_example/query/ListAllPokemons
- http://localhost:8080/latest/jpars_example/query/ListAllTrainers

Specific instance of entity may be retrieved using `http://localhost:8080/latest/jpars_example/entity/<entity_name>/<id>`:
- http://localhost:8080/latest/jpars_example/entity/Type/1
- http://localhost:8080/latest/jpars_example/entity/Pokemon/1
- http://localhost:8080/latest/jpars_example/entity/Trainer/1

Entity modifications may be done using
- `PUT` request to create a new instance of entity:
```
 curl -X PUT -H "Content-Type: application/json" \
       -d '{"id":20,"name":"Gloom","typeId":12}' \
       http://localhost:8080/latest/jpars_example/entity/Pokemon
```