## 1. Docker run
This is command for create docker-image
```shell script
Docker image build . -t docker.io/teiwas/short-url:1.0.0
```
This is command for Docker image run
```shell script
Docker run --env-file src\main\resources\application-development.properties -p 8080:8080 test
```



## 2. Minikube run
This is command for minikube run
```shell script
minikube start 
```
This is command for running service in minikube
```shell script
skaffold run
```
Service port forward
```shell script
kubectl port-forward svc/short-url 8080:8080 
```
Delete all
```shell script
skaffold delete 
```