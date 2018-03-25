Application to track table soccer games. This application is based on spring boot, thymeleaf and bootstrap.
Game data is stored into mongodb. Glicko2 rating is based on https://github.com/forwardloop/glicko2s

## How-To
To build the project just run "gradlew build" inside the project root. Start application with "gradlew bootRun". Due to the _gradle wrapper_ you does not have to install _gradle_ before. Open **localhost:8080**

If no MongoDB is defined, the application expects a running instance under the standard port localhost:27017

If run a mongodb instance on a separate machine, then you have to define an application property file, were you define monogdb connection:

| Property                   | Description |
| -------------------------- | ------------- |
| spring.data.mongodb.host:  | Host name of the mongodb server |

Before a new match can be added, the players must first be created. These can then be selected from the drop-down menu. The API is currently used to create a new user:

| Method | Path | Description |
| ------ | ---- | ----------- |
| GET    | /api/players | returns all players |
| POST   | /api/players?name=<name> | creates a new player. the name is specified by query variable |


Sites:

| URL | Description |
| --- | ----------- |
| /   | home |
| /matches | Overview of matches |
| /players/#player name# | Player related information |

## ToDo
* Show rating as graph
* Build into docker
* User management
* Security, User login
* Optimize html pages
* ...
