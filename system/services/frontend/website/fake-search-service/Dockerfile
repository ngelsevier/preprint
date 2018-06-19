FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/java:development
COPY supervisord.conf /etc/supervisor/conf.d/
WORKDIR /opt/service/classpath
COPY out/production/external-runtime-libraries external-libraries
COPY out/production/internal-runtime-libraries internal-libraries
COPY out/production/classes classes
ARG version=local
ENV VERSION $version