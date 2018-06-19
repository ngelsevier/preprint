FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/java:development
COPY supervisord.conf /etc/supervisor/conf.d/
WORKDIR /opt/service/classpath
COPY out/production/external-runtime-libraries external-libraries
COPY out/production/internal-runtime-libraries internal-libraries
COPY out/production/classes classes
COPY out/production/resources resources
RUN setcap 'cap_net_bind_service=+ep' $(readlink -e $(which java))
ARG version=local
ENV VERSION $version