# Deploy with Kubernetes

## Prerequisites

Official [cBioPortal Helm chart](https://artifacthub.io/packages/search?org=cbioportal) can be used to easily deploy an instance on a Kubernetes cluster. Make sure you meet the following prerequisites before continuing with the usage instructions:

- You have access to a cluster (e.g. Minikube or AWS EKS). We recommend [setting up a Minikube cluster](https://minikube.sigs.k8s.io/docs/start/) on your local machine for development purposes.
- You have installed [Helm](https://helm.sh/docs/intro/install/) on your system.
- You have read and write access to a mysql database server.

## Usage instructions


### Cluster & Database Setup

#### Step 1 - Add cBioPortal label to your cluster

Make sure your cluster is already set up and you have access to a node running on it. Instructions for this can vary, depending on your Kubernetes provider. Once your cluster is active, run the following command to add a label to the node on your cluster.

```
kubectl label nodes <your-node-name> node-group=cbioportal
```

#### Step 2 - Export database access credentials
cBioPortal needs access to a mysql database server hosting cancer study data. As mentioned in the prerequisites, you need access to a mysql database server for this. Instructions for this can vary, depending on your database server provider. Once you have a server available, download MSK's latest database dump [here](https://public-db-dump.assets.cbioportal.org/) and add the data to your database server. Then, continue with the instructions below using your mysql server credentials.

Create a new values file called _values.secret.yaml_ and add your database credential values.
```yaml
container:
    env:
        - name: DB_USER
          value: <your-db-user>
        - name: DB_PASSWORD
          value: <your-db-password>
        - name: DB_CONNECTION_STRING
          value: <your-db-connection_string>
```

### Install cBioPortal

Now that your cluster and data sources have been successfully configured, you can install the cBioPortal helm chart.

#### Step 1 - Install Helm Chart

Add repository.
```
helm repo add cbioportal https://cbioportal.github.io/cbioportal-helm/
```

Install chart
```
helm install my-cbioportal cbioportal/cbioportal --version 0.1.6 -f path/to/values.secret.yaml
```

You should see something similar to this, indicating that the installation was successful.
```
NAME: my-cbioportal
LAST DEPLOYED: Thu Nov 14 14:15:18 2024
NAMESPACE: default
STATUS: deployed
REVISION: 1
TEST SUITE: None
```

#### Step 2 - Access cBioPortal through localhost
Run the following command to port-forward cBioPortal from the cluster to your local network.
```
kubectl port-forward deployment/cbioportal 10000:8080
```

cBioPortal should now be available at localhost on port 10000. Navigate to [http://localhost:10000](http://localhost:10000) in your browser to view it.
