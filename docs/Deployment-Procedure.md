# Deployment Procedure
This describes our internal deployment procedure. Shared publicly, in case it
may be of use. Instructions on how to deploy cBioPortal can be found elsewhere,
see e.g. [Deploying the web application](Deploying.md) and [Deploy using
Docker](Deploy-Using-Docker.md).

We deploy the master branch of backend and the master branch of frontend to
production. The public portal (https://www.cbioportal.org) runs on AWS inside
kubernetes. The configuration can be found in the knowledgesystems repo:

https://github.com/knowledgesystems/knowledgesystems-k8s-deployment 

Other portals run at MSKCC on two internal machines called dashi and dashi2.
Since we're running several apps in several tomcats internally the procedure
for updating them is different from the public portal on AWS. The configuration
is in the mercurial portal-configuration repo. To make changes, ask Ben for
access. 

The frontend and backend can be upgraded independently. We have the following
events that can require a new deployment:

1. [New frontend commit in master](new-frontend-commit-in-master)
1. [New backend commit in master](new-backend-commit-in-master)

## New frontend commit in master
Currently we are auto-deploying the frontend master branch to netlify:
https://cbioportal-frontend.netlify.com. So any change should be automatically
built and deployed to the relevant portals if the frontend configuration has
been set up properly. Do note that the current build time for the frontend
project is ~15 minutes or so. To see what frontend commit is deployed, check
`window.FRONTEND_COMMIT` in the console of the browser.

### Public Portal Frontend URL
The public portal is on AWS and running inside a Kubernetes cluster.
The URL that it gets the frontend version from is here:

https://github.com/knowledgesystems/knowledgesystems-k8s-deployment/search?q=-Dfrontend.url&unscoped_q=-Dfrontend.url

This should be a URL pointing to netlify.

### Internal Portal Frontend URL
For the internally runnning portals the frontend.url is defined in the
portal.properties file in the mercurial portal-configuration repo. If set up
correctly, this should point to a file on both dashi and dashi2 that in turn
points to a netlify frontend URL. The reason we have a separate file with the
URL in it is that it allows us to update the frontend URL without redeploying
the backend.

## New backend commit in master
A new backend commit usually also means a new frontend change is necessary. For
this reason the following sections assume that's the case.

### Public Portal Backend Upgrade
Once the backend repo has been tagged on github, a docker image gets build on Docker Hub automatically. It can take ~20m or so before the image is available. You can check here what the status of the builds is: https://hub.docker.com/r/cbioportal/cbioportal/builds.


After that, if you have access to the kubernetes cluster you can change the image in the configuration of the kubernetes cluster:


https://github.com/knowledgesystems/knowledgesystems-k8s-deployment/blob/master/cbioportal/cbioportal_spring_boot.yaml

point this line, to the new tag on docker hub e.g.:

```
image: cbioportal/cbioportal:3.0.3-web-shenandoah
```

Make sure it is an image with the postfix `-web-shenandoah`. This is the image that only has the web part of cBioPortal and uses the shenandoah garbage collector.

Also remove the `-Dfrontend.url` parameter such that the frontend version inside the war will be used:

```
"-Dfrontend.url=https://frontend.cbioportal.org"
```

Then running this command applies the changes to the cluster:

```
kubectl apply -f cbioportal/cbioportal_spring_boot.yaml
```

You can keep track of what's happening by looking at the pods:

```
kubectl get po
```

If you have the watch command installed you can also use that to see the output
of this every 2s:

```
watch kubectl get po
```

Another thing to look at is the events:

```
kubectl get events --sort-by='{.lastTimestamp}'
```

If there are any issues, point the image back to what it was, set
`-Dfrontend.url` and run `kubectl apply -f filename` again.

If everything went ok, you can re-enable auto deployment on netlify, set
`-Dfrontend.url` in the kubernetes file and run `kubectl apply -f filename`
again.

Make sure to commit your changes to the knowledgesystems-k8s-deployment repo
and push them to the main repo, so that other people making changes to the
kubernetes config will be using the latest version. 

### Private Portal Backend Upgrade
First update the frontend portal configuration to point to a new file. It's
fine if this file does not exist yet, because if it doesn't the frontend
bundled with the war will be used. We can later point the file to netlify, once
we've determined everything looks ok.

You can use this for loop to update the frontend url in all properties files
(set it to a file that doesn't exist yet and give it a sensible name e.g. `frontend_url_version_x_y_z.txt`):

```
for f in $(grep frontend.url.runtime properties/*/portal.properties | grep -v beta | cut -d: -f1); do sed -i 's|frontend.url.runtime=/srv/www/msk-tomcat/frontend_url_version_2_0_0.txt|frontend.url.runtime=/srv/www/msk-tomcat/frontend_url_version_2_1_0.txt|g' $f; done
```
Same for triage-tomcat (agin set the correct file name)::

```
 for f in $(grep frontend.url.runtime properties/*/portal.properties | grep -v beta | cut -d: -f1); do sed -i 's|frontend.url.runtime=/srv/www/triage-tomcat/frontend_url_version_2_0_0.txt|frontend.url.runtime=/srv/www/triage-tomcat/frontend_url_version_2_1_0.txt|g' $f; done
```	
Make sure you see the frontend url file updated correctly:

```
hg diff
```

Then commit and push your changes to the mercurial repo:
```
hg commit -u username -m 'update frontend url files for new release'
hg push
```

If you have your public key added for the relevant deploy scripts you should be able to deploy with the following command on dashi-dev:

```
# set PROJECT_CONFIG_HOME and PORTAL_HOME to your own directory
unset PROJECT_VERSION && export PORTAL_HOME=/data/debruiji/git/cbioportal && export PORTAL_CONFIG_HOME=/data/debruiji/hg/portal-configuration && cd ${PORTAL_CONFIG_HOME}/buildwars && hg pull && hg update && export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk.x86_64 && bash buildproductionwars.sh master && bash ${PORTAL_CONFIG_HOME}/deploywars-remotely/deployproductionportals.sh
```

If you don't have a SSH key set up to run the deploy script ask Ino.

If everything looks ok you can update the frontend url file to point to
netlify. Log in to dashi and become msk-tomcat with `sudo su - msk-tomcat`.
Then change the update script:

```
vi /data/cbio-portal-data/portal-configuration/deploy-scripts/updatefrontendurl.sh
```
to point `oldurlfile=/srv/www/msk-tomcat/frontend_url_version_2_0_0.txt` to the
new frontend url file you supplied above.

Then update the url like:

```
./updatefrontendurl.sh "https://cbioportal-frontend.netlify.com"
```

Do the same thing on dashi2. 

The last step is to modify the frontend url file for the triage portal. Log in to the pipelines machine, log in as triage-tomcat user: `sudo su - triage-tomcat`, and update the frontend url file there:

```
echo 'https://cbioportal-frontend.netlify.com' > /srv/www/triage-tomcat/frontend_url_version_2_1_0.txt
```
