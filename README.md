Application to track table soccer games. This is based on spring boot. The UI is based on thymeleaf and bootstrap.
Glicko2 rating is based on https://github.com/forwardloop/glicko2s

Very draft version.

# How-To
To build the project just run "gradlew build" inside the project root. No need to install grade. 

You have to define an application property file, were you define monogdb connection. At leas following properties have to be set:
|Property                   | Description |
|---------------------------|-------------|
|spring.data.mongodb.host:  | Host name of the mongodb server |

# ToDo
* Add glicko evaluation
* Show score board
* Show rating as graph
* Build into docker
* Security
* ...
