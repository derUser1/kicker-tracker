Application to track table soccer games. This application is based on spring boot, thymeleaf and bootstrap.
Glicko2 rating is based on https://github.com/forwardloop/glicko2s

Very draft version.

# How-To
To build the project just run "gradlew build" inside the project root. No need to install grade. 

You have to define an application property file, were you define monogdb connection. At leas following properties have to be set:
| Property                   | Description |
| -------------------------- | ------------- |
| spring.data.mongodb.host:  | Host name of the mongodb server |

Before a new match can be added, the players must first be created. These can then be selected from the drop-down menu. Here, api
can be used:

| Method | Path | Description |
| ------ | ---- | ----------- |
| GET    | /api/players | returns all players |
| POST   | /api/players?name=<name> | creates a new player. the name is specified by query variable |


# ToDo
* Add glicko evaluation
* Show score board
* Show rating as graph
* Build into docker
* User management
* Security, User login
* Optimize html pages
* ...
