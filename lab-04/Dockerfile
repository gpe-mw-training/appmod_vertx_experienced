# Build:
#   docker build --rm -t catalog-service-tracing:1.0.5 .
#   docker tag catalog-service-tracing:1.0.5 docker.io/rhtgptetraining/catalog-service-tracing:1.0.5

# Docker push (on F28):
#   Make sure the following is the *first* entry in the docker daemon (by updating /etc/sysconfig/docker):  --add-registry docker.io
#   docker login docker.io -u rhtgptetraining
#   docker push docker.io/rhtgptetraining/catalog-service-tracing:1.0.5

# Execution: OpenShift
#   oc run catalog-service-tracing --image=docker.io/rhtgptetraining/catalog-service-tracing:1.0.5

FROM fabric8/java-jboss-openjdk8-jdk:1.3.1
ENV JAVA_APP_DIR=/deployments
ENV AB_OFF=true
ENV JAEGER_SERVICE_NAME=catalog\
  JAEGER_ENDPOINT=http://jaeger-collector.istio-system.svc:14268/api/traces\
  JAEGER_PROPAGATION=b3\
  JAEGER_SAMPLER_TYPE=const\
  JAEGER_SAMPLER_PARAM=1
EXPOSE 8080 8778 9779
COPY target/catalog-service-tracing-1.0.5.jar /deployments/
