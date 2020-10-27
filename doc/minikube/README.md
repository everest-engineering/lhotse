
# Minikube 

[Minikube](https://github.com/kubernetes/minikube) is a simple way of installing and managing a local Kubernetes installation using virtual machines.

Install Minikube and then configure it with additional resources:

`minikube config set cpus 4`

`minikube config set memory 4096`

Then start a new node with `minikube start`.

Enable container registry: `minikube addons enable registry`

Finally, enable Minikube ingress so that you can connect to the cluster: `minikube addons enable ingress`

The local Kubernetes node will have its own internal container registry. To access this registry
(so that containers can be published to it), run `eval $(minikube docker-env)`. See the section below on how to allow Kubernetes to access a private repository.

## Deployments

Execute the following development only stateful sets:

`kubectl apply -f dev/postgres-db.yaml`

`kubectl apply -f dev/mongo-gridfs.yaml`

`kubectl apply -f dev/mongo-express.yaml`

`kubectl apply -f dev/hazelcast-discovery-service.yaml`


Create development config maps and secrets for web-app application:

`kubectl apply -f dev/web-app-config.yaml`

`kubectl apply -f dev/web-app-secret.yaml`


To deploy the backend application:
 
  * Execute `eval $(minikube docker-env)` to point your shell to Minikube's Docker daemon
  * Build a container image following instructions in the top level README.md
  * Update the version that will be deployed in `dev/web-app.yaml`
  * Execute `kubectl apply -f dev/web-app.yaml`

You can monitor the progress of deployments using the dashboard: `minikube dashboard`

## Accessing services 

Once up an running, the application will be available on the IP address set up by Minikube: `minikube ip`

To find the IP address of the various services:

`kubectl get service`

 Run `minikube tunnel` to set up a tunnel to access these via their cluster IP address.
