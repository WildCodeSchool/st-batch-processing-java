## Install Spring Cloud Dataflow

`wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-server/2.9.1/spring-cloud-dataflow-server-2.9.1.jar`

## Install Docker PostgreSQL

`docker run -p5432:5432 --name batch-postgres -e POSTGRES_PASSWORD=q -d postgres`

## Env

```
export SPRING_CLOUD_DATAFLOW_FEATURES_STREAMS_ENABLED=false
export SPRING_CLOUD_DATAFLOW_FEATURES_SCHEDULES_ENABLED=false
export SPRING_CLOUD_DATAFLOW_FEATURES_TASKS_ENABLED=true
export spring_datasource_url=jdbc:postgresql://localhost:5432/tasklistdb
export spring_datasource_username=postgres
export spring_datasource_password=q
export spring_datasource_initialization_mode=always
```

export spring_datasource_driverClassName=org.mariadb.jdbc.Driver
