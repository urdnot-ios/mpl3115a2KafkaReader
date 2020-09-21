#!/bin/zsh

# did you change the version number?
sbt clean
sbt assembly
sbt docker:publishLocal
docker image tag mpl3115a2kafkareader:latest intel-server-03:5000/mpl3115a2kafkareader
docker image push intel-server-03:5000/mpl3115a2kafkareader

# Server side:
# kubectl apply -f /home/appuser/deployments/mpl3115Reader.yaml
# If needed:
# kubectl delete deployment iot-mpl3115-kafka-reader
# For troubleshooting
# kubectl exec --stdin --tty iot-mpl3115-kafka-reader -- /bin/bash
