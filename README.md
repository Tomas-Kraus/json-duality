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
Database related profiles may be used to start and stop the database container in `pre-integration-test`
and `post-integration-test` phases. So all profiles will be executed as part of `verify` goal.

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
  
## SQL limitations

JSON-Relational Duality view SQL has strong limitations. Only `FROM <table> WHERE <join_clause>` is allowed. Even ordering
of result is not allowed as part of duality view definition.
See [duality view syntax grammar](https://docs.oracle.com/en/database/oracle/oracle-database/23/sqlrf/create-json-relational-duality-view.html#GUID-64B579AD-BF97-4B27-BF22-94C1FB6FD6DF) for more details.

# Oracle 23c Mongo API

MongoDB API support is part of [Oracle REST Data Services](https://docs.oracle.com/en/database/oracle/mongodb-api/index.html) (ORDS) package.
This package must be installed into the database to enable this feature.

## MongoDB API support in Helidon example

HTTP service `MongoService` uses MongoDB Java API to connect to the Oracle database via MongoDB API support and to execute CRUD operations
on default document collection.

To see the default colelction structure, just connect to the database with `sqlplus` and execute `DESCRIBE POKEMON_DOCUMENTS;`. This will show
that default collection table is just raw JSON storage.

### Read operations

There are two read services available:
- get single pokemon by ID:
  ```
  curl http://localhost:8080/mongo/get/<id>
  ```
- list all pokemons in the database:
  ```
  curl http://localhost:8080/mongo/list
  ```
 
### Insert operation

New pokemon may be inserted using:
```
curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Gloom","type_id":12}' http://localhost:8080/mongo/insert
```

### Update operation

Existing pokemon may be modified using:
```
curl -X POST -H "Content-Type: application/json" -d '{"_id":20,"name":"Vileplume","type_id":4}' http://localhost:8080/mongo/update
```

### Delete operation

Existing pokemon may be deleted using:
```
curl -X POST -H "Content-Type: application/json" -d '[20]' http://localhost:8080/mongo/delete
```

### Using read operation on existing JSON-Relational Duality view

access to JSON-Relational Duality is possible trough MongoDB API support. But not directly. Additional structure must be created
to map this wiew to MongoDB collection:
```
DECLARE pokemon_collection soda_collection_t;
BEGIN
    pokemon_collection := DBMS_SODA.create_dualv_collection('pokemon_mongo', 'POKEMONS');
END;
/
```

Now POKEMONS JSON-Relational Duality view is seen as pokemon_mongo collection using MongoDB Java API.

To list all pokemons using this wiew, call listView service:
```
curl http://localhost:8080/mongo/listView
```
