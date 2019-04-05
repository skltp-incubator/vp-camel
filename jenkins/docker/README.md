# Dockerized Jenkins with Maven
This folder contains everything needed to run Jenkins in a Docker container, with Maven pre-configured. The Dockerfile installs Jenkins and Maven. The environment variable `_JAVA_OPTIONS_` is specified to avoid the bug with Maven's surefire plugin, as discussed here: https://stackoverflow.com/a/53085816. The easiest way to get up and running is by using the docker-compose file.

## 1. Directory structure

| path  | description   |
|---|---|
| README.md | This file |
| docker-compose.yml | The docker compose file used to specify ports, environment variables etc. |
| Dockerfile | The file used to generate a docker image |
| .env | The environment variables used. The docker-compose file reads values from here. |

## 2. Versions
| Jenkins | Maven  |
|---|---|
| Latest monthly build | Latest |

## 3. Get up and running
Choose one of the following methods to create and run a Jenkins docker container.

### 3.1. Commands to start from docker-compose
esetExecute these commands in the folder where this README and `docker-compose.ýml` file are located.
1. `docker-compose up`. This command build the docker image and starts the Jenkins docker container.

### 3.2 Commands to start from Dockerfile
Execute the commands in order from this directory. These steps are equal to running the `docker-compose up` in the previous paragraph.

1. Create a docker image named `jenkins-image` from the Dockerfile: 
	* `docker build -t jenkins-image .`
2. Create and run a docker container named `jenkins-container` based on the `jenkins-image` from step 1. The `-v` flag is used to create a volume on the host to persist data. The `-e` flag is used to specify an environment variable. Here, the `TZ` variable specifies the timezone for the server.  
	* `docker run -p 8080:8080 -p 50000:50000 -e _JAVA_OPTIONS_=-Djdk.net.URLClassPath.disableClassPathURLCheck=true -e TZ=Europe/Stockholm -v jenkins_home:/var/jenkins_home
jenkins-image`

## 4. Tips

1. To copy data from a docker container to your local computer, use the `docker cp CONTAINER:SRC_PATH DEST_PATH` command. For example, to copy a Jenkins job's config file, run the following command: `docker cp jenkins-container:/var/jenkins_home/jobs/my-job/config.xml C:\Jenkins\jobs\my-job`
