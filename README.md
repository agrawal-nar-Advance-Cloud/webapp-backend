# Webapp Backend API

## Objective
Creating REST Spring Boot API

### Maintainer 
<table>
    <thead>
      <tr>
        <th>Name</th>
        <th>NUID</th>
      </tr>
    </thead>
    <tbody>
        <tr>
            <td>Naresh Agrawal</td>
            <td>001054600</td>
        </tr>
    </tbody>
</table>

## Technology Stack
* Spring Boot
* MYSQL 
* Helm

## Prerequisites
* JAVA(JDK)
* Docker
* Helm

## Running application locally
- Run following commands in the root directory
```
$ mvn package
$ cd target
$ java -jar backend-latest.jar
```
- Set Environment variables
```
RDS_CONNECTION_STRING=localhost:3306
RDS_DB_NAME=<DB_Name>
RDS_USER_NAME=<DB_User_Name>
RDS_PASSWORD=<DB_Password>
```

* Shutdown application locally
```
$ ctrl+c
```

## Build Docker image
```
$ docker build -t <image-name> .
```

## Run application by Docker image
```
$ docker run --rm -p 8080:8080 --network=host -e RDS_CONNECTION_STRING=localhost:3306 -e RDS_DB_NAME=<DB_Name> -e RDS_USER_NAME=<DB_User_Name> -e RDS_PASSWORD=<DB_Password> <image-name>
```
The application should be running and listening for HTTP requests on port 8080 on localhost
http://localhost:8080/

## Deploy application on Kubernetes Cluster
```
$ helm install backend ./helm/
```

## Uninstall application on Kubernetes Cluster
```
$ helm delete backend
```
