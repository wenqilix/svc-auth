#!/bin/bash
case "$ENV" in
  development)
    IMAGE_NAME='mcf/svc-auth';
    MEMORY_LIMIT='1Gi';
    MEMORY_REQUEST='600Mi';
    REPLICA='1';
    CERTS="local-svc-auth-certs";
    ;;
  qa|remote-development)
    IMAGE_NAME='mcf/svc-auth';
    MEMORY_LIMIT='1Gi';
    MEMORY_REQUEST='600Mi';
    REPLICA='1';
    ;;
  uat)
    IMAGE_NAME='mcf/svc-auth';
    MEMORY_LIMIT='1Gi';
    MEMORY_REQUEST='600Mi';
    REPLICA='1';
    ;;
esac

CERTS="${CERTS:-svc-auth-certs}"
echo "version ${VERSION}"
echo "env ${ENV}"

cat << EOF > temp.yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: svc-auth-${ENV}
  namespace: ojmp
  labels:
    app: mcf
    env: ${ENV}
    type: auth
spec:
  replicas: ${REPLICA}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 100%
  template:
    metadata:
      name: svc-auth-${ENV}
      namespace: ojmp
      labels:
        app: mcf
        env: ${ENV}
        type: auth
    spec:
      containers:
      - image: nexus-docker.gahmen.tech/${IMAGE_NAME}:${VERSION}
        name: svc-auth
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: ${ENV}
        - name: APP_TOKEN_PRIVATE_KEY
          value: "${APP_TOKEN_PRIVATE_KEY}"
        - name: APP_SINGPASS_SERVICE_PROVIDER_PRIVATE_KEY
          value: "${APP_SINGPASS_SERVICE_PROVIDER_PRIVATE_KEY}"
        - name: APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST
          value: "${APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST}"
        - name: APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT
          value: "${APP_SINGPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT}"
        - name: APP_CORPPASS_SERVICE_PROVIDER_PRIVATE_KEY
          value: "${APP_CORPPASS_SERVICE_PROVIDER_PRIVATE_KEY}"
        - name: APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST
          value: "${APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_HOST}"
        - name: APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT
          value: "${APP_CORPPASS_IDENTITY_PROVIDER_ARTIFACT_SERVICE_PROXY_PORT}"
        - name: APP_TOKEN_EXPIRATION_TIME
          value: "${APP_TOKEN_EXPIRATION_TIME}"
        - name: APP_ZIPKIN_INSTRUMENTATION_ZIPKIN_URL
          value: "https://mcf-zipkin.gds-gov.tech"
        resources:
          limits:
            cpu: 150m
            memory: ${MEMORY_LIMIT}
          requests:
            cpu: 50m
            memory: ${MEMORY_REQUEST}
        ports:
        - containerPort: 8000
        volumeMounts:
        - name: certs
          readOnly: true
          mountPath: "/app/shared/certs"
        - name: metadata
          readOnly: true
          mountPath: "/app/shared/metadata"
      imagePullSecrets:
      - name: docker-secret
      volumes:
      - name: certs
        secret:
          secretName: ${CERTS}
      - name: metadata
        secret:
          secretName: svc-auth-metadata
EOF

kubectl apply -f temp.yaml
