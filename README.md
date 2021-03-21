# Widget service
[![Build Status](https://travis-ci.com/M-Razavi/Widget-Service.svg?branch=main)](https://travis-ci.com/github/M-Razavi/Widget-Service)
[![Coverage Status](https://coveralls.io/repos/github/M-Razavi/Widget-Service/badge.svg?branch=main)](https://coveralls.io/github/M-Razavi/Widget-Service?branch=main)

A web service to work with widgets on a board via HTTP REST API.The service stores only widgets and assuming that all clients work with the same board.

### Glossary
A Widget is an object on a plane in a Cartesian coordinate system that has coordinates (X, Y), Z-index, width, height, last modification date, and a unique identifier.   

### Conditions
* X, Y, and Z-index are integers (may be negative). Width and height are integers > 0.
Widget attributes should be not null  
* A Z-index is a unique sequence common to all widgets that
  determines the order of widgets (regardless of their coordinates).
  Gaps are allowed. The higher the value, the higher the widget
  lies on the plane.
    
### Considerations  
* Two spring profiles for supporting in memory repository or any SQL database without need to touch the code (`h2` , `memory` - by default use memory storage )  
* Optimistic locking for updates  
* Separating entity from request/response model with  MapStruct

### Run
1. `$ mvn spring-boot:run`  Runs the application with in Memory storage profile

2. `$ mvn spring-boot:run -Dspring-boot.run.profiles=h2` Runs the application with in H2 Database storage profile  

The API gauid could be found http://127.0.0.1:8080/swagger-ui.html
  
### Technologies  
    Java 11
    Spring boot & Rest
    Junit5
    Mockito And MockMVC
    Openpojo
    Apache commons
    Lombok
    MapStruct
    Swagger
    Log4j2

