# Packaging Multiple SpringBoot Services into a Single Docker Container

Container-based application design encourages that there should just be one process running in a container.
This way if your load increases you can spawn more instances of the processes by starting more than one containers.
A container’s main running process is the ENTRYPOINT and/or CMD at the end of the Dockerfile.

However, there can be instances where you need more than one processes to run inside a single Docker Container.

This project is an example of such a packaging strategy.

Here there are two services
1) Customer Service: This is a spring-boot based application which provides REST API endpoints to add customers, get total count of customers enrolled and fetch the customer name by id.
2) Lucky Winner : This is another spring-boot aplication which uses the customer service and chooses a lucky winner randomly.

There are two approaches to run  multiple processes inside a single Docker container.

## 1) Using Wrapper Script
In this approach we create a wrapper shell script and put the commands to start both the spring boot applications inside it.

Run the wrapper script as your ENTRYPOINT. 

```shell 
script.sh
java -jar /app/customer-service-*.jar & java -jar /app/lucky-winner-*.jar
```

Dockerfile:
```dockerfile
FROM openjdk:8
COPY customer-service/target/customer-service-*.jar /app/
COPY lucky-winner/target/lucky-winner-*.jar /app/
COPY script.sh /app
RUN chmod +x /app/script.sh

EXPOSE 8081
EXPOSE 8082

ENTRYPOINT ["sh" , "/app/script.sh"]

```

Build the docker container
`docker build -t multi-process-docker:1 .`

Run the container

`docker run -p 8081:8081 -p 8082:8082  multi-process-docker:1`

## 2) Using process manager like supervisor-d
In this approach we are required to package supervisord and its configuration in your image along with the different applications it manages. Then you start supervisord, which manages your processes.

Sample supervisor.conf
```shell
[unix_http_server]
file=/tmp/supervisor.sock   ; the path to the socket file
;chmod=0700                 ; socket file mode (default 0700)
;chown=nobody:nogroup       ; socket file uid:gid owner
username=admin              ; default is no username (open server)
password=admin              ; default is no password (open server)

[inet_http_server]         ; inet (TCP) server disabled by default
port=127.0.0.1:9001       ; ip_address:port specifier, *:port for all iface
username=admin              ; default is no username (open server)
password=admin               ; default is no password (open server)

[supervisord]
logfile=/tmp/supervisord.log ; main log file; default $CWD/supervisord.log
logfile_maxbytes=50MB        ; max main logfile bytes b4 rotation; default 50MB
logfile_backups=10           ; # of main logfile backups; 0 means none, default 10
loglevel=info                ; log level; default info; others: debug,warn,trace
pidfile=/tmp/supervisord.pid ; supervisord pidfile; default supervisord.pid
nodaemon=true               ; start in foreground if true; default false
minfds=1024                  ; min. avail startup file descriptors; default 1024
minprocs=200                 ; min. avail process descriptors;default 200
;umask=022                   ; process file creation umask; default 022
user=root            ; setuid to this UNIX account at startup; recommended if root

[rpcinterface:supervisor]
supervisor.rpcinterface_factory = supervisor.rpcinterface:make_main_rpcinterface

[supervisorctl]
serverurl=unix:///tmp/supervisor.sock ; use a unix:// URL  for a unix socket
serverurl=http://127.0.0.1:9001 ; use an http:// url to specify an inet socket

[program:customer-service]
command=java -jar /app/customer-service-0.0.1-SNAPSHOT.jar
startsecs=10
directory=/app
stdout_logfile=/app/customer-service.log
stdout_logfile=/app/customer-service.err


[program:lucky-winner-service]
command=java -jar /app/lucky-winner-0.0.1-SNAPSHOT.jar
startsecs=10
directory=/app
stdout_logfile=/app/lucky-winner-service.log
stdout_logfile=/app/lucky-winner-service.err


```
Dockerfile:
```shell
FROM ubuntu:latest
RUN apt-get update && apt-get install -y supervisor && apt-get install -y openjdk-8-jdk
RUN mkdir -p /var/log/supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY customer-service/target/customer-service-*.jar /app/
COPY lucky-winner/target/lucky-winner-*.jar /app/

EXPOSE 8081
EXPOSE 8082
EXPOSE 9001

ENTRYPOINT ["/usr/bin/supervisord"]
```

Build the docker image:
`docker build -t multi-process-docker:1 -f Dockerfile-supervisord .`

Start the docker container
`docker run -p 8081:8081 -p 8082:8082 -p 9001:9001 multi-process-docker:1`



## References:
https://docs.docker.com/config/containers/multi-service_container/


