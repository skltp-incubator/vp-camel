# Jenkins
Jenkins jobs configured with pipelines and webhooks for vp-camel

## 1. Directory structure

| path  | description   |
|---|---|
| README.md | This file |
| /docker | Contains files needed to get an instance of Jenkins up and running in a docker container|
| /jobs | Contains all the Jenkins jobs for vp-camel, and their config and Jenkinsfiles |

## 2. Overview
All Jenkins jobs use the pipeline plugin to read a Jenkinsfile (one for each pipeline). Some jobs are configured to build periodically, but they can also be triggered by a Github webhook (a HTTP post request sent from the github repository) when a certain criteria is fulfilled, such as a push to the repository. 

## 3. Github webhooks

### 3.1 Setup
Instead of letting Jenkins poll the github repository for changes, the responsibility to inform Jenkins of code changes can be moved to the repository itself. This is done by the use of Github webhooks. In short, the URL to the Jenkins instance is configured with the `/github-webhook/` endpoint in Github. This URL will be used by Github to push a notification to Jenkins that something has been updated. A good source regarding setting up webhooks can be found here: https://www.blazemeter.com/blog/how-to-integrate-your-github-repository-to-your-jenkins-project. A webhook can not be specified on a per branch basis, so what branches to check for updates has to be configured in each Jenkins job. A webhook will send a payload containing information regarding what branch has been updated, who made the commit and other useful information. In most cases, we do not need to worry about the payload explicitly in Jenkins. Checking what branch has been updated will be performed automatically. Note that the initial build needs to be performed manually in Jenkins, since the webhooks do not seem to trigger builds until one build has been performed.

### 3.2 Testing webhooks integration using ngrok
If you are running Jenkins on your `localhost`, Github will not be able post any webhooks. A workaround is to use the `ngrok` application to create a public URL to your Jenkins server. Be aware that this opens up the Jenkins server to anyone with the URL. This procedure should not be used when working with sensitive information, but can be useful for testing purposes. In order to isolate the ngrok application, and have easy access to turn it on/off, the suggestes method is to run ngrok in a docker container. Depending on if you are running Jenkins in a docker or not, the setups differ slightly. The two methods are specified below.

#### 3.2.1 Ngrok-docker to Jenkins-docker
If your Jenkins instance is running in a docker container, follow the steps below to setup an ngrok docker container.
We will use the ngrok docker image found here: https://github.com/gtriggiano/ngrok-tunnel

1. Create a new docker network that can be shared between Jenkins and ngrok
`docker network create my-network`
2. Run the steps in the README in the /docker subfolder to create and run a docker container, but make sure to include `--net my-network` in the `docker run` command. This will add the Jenkins docker container to the network we created in step 1. 
3. Pull the ngrok docker image
`docker pull gtriggiano/ngrok-tunnel`
4. Run the ngrok docker container. `TARGET_HOST` should be the name of your Jenkins docker container from step 2. The `TARGET_PORT` should be the port that Jenkins listens to, usually `8080`.
`docker run -it --name jenkins-ngrok -e "TARGET_HOST=jenkins-container" -e "TARGET_PORT=8080" -p
4040:4040 --net my-network gtriggiano/ngrok-tunnel`
5. The ngrok docker container logs will specify the public url that has been created. Try to navigate to the URL in your browser. You should reach the Jenkins login prompt.
6. In Github, when setting up the url to Jenkins, take the public URL created in step 5 and paste it into the textbox, followed by `/github-webhook/`. Note that it is important to include the trailing `/`, otherwise the webhooks will fail.

### 3.2.2 Ngrok-docker to Jenkins on local machine
If your Jenkins instance is running on your machine, and not in a docker container, you can follow steps 3 to 6 from the previous paragraph. However, make the following changes:
1. Omit the `--net my-network` part in the `docker run` command. It is not needed since we are only running one docker container. 
2. Set `TARGET_HOST` to `localhost`. Change `TARGET_PORT` to the port your Jenkins instance is listening to.

