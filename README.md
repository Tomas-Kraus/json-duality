# JSON-Relational Duality Trials and Errors

## Helidon with JDBC and DB view example

```
Usage:
    mvn clean verify -Phelidon,startdb,stopdb
```

Profiles:
- `helidon`: Start HTTP service (Helidon)
- `startdb`: Start Oracle 23c database in docker container and initialize DB schema and data
- `stopdb`: Stop Oracle 23c database in docker container

HTTP service profile starts HTTP server during `integration-test` phase.
Database related profiles may be used to start and stop the database container in `pre-integration-test` and `post-integration-test` phases.
So all profiles will be executed as part of `verify` goal.

Helidon HTTP server can be turned off using `curl http://localhost:8080/exit`

Following command may be used to call REST API `GET` request with JSON response using `curl`:
- `curl -H "Content-Type: application/json" <url>`

Listing services:
- get all trainers ant their pokemons:
  ```
  curl -H "Content-Type: application/json" http://localhost:8080/pokemon/trainers
  ```
- get all pokemons:
  ```
  curl -H "Content-Type: application/json" http://localhost:8080/pokemon/list
  ```

Data modification services:
- insert new pokemon
  ```
  curl -X POST -H "Content-Type: application/json" \
       -d '{"id":20,"name":"Gloom","type_id":12}' http://localhost:8080/pokemon/insert
  ```
- update pokemon
  ```
  curl -X POST -H "Content-Type: application/json" \
       -d '{"id":20,"name":"Vileplume","type_id":4}' http://localhost:8080/pokemon/update
  ```
- delete pokemon
  ```
  curl -X POST -H "Content-Type: application/json" \
       -d '[20]' http://localhost:8080/pokemon/delete
  ```