ARG parent_image_version=development
FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/java:${parent_image_version}
RUN apt-get update && \
    apt-get --assume-yes install locales && \
    apt-get install -y curl && \
    curl -L -O https://artifacts.elastic.co/downloads/beats/filebeat/filebeat-5.5.2-amd64.deb && \
    dpkg -i filebeat-5.5.2-amd64.deb
COPY supervisord.conf /etc/supervisor/conf.d/
COPY filebeat/filebeat.yml /etc/filebeat/
RUN chmod 644 /etc/filebeat/filebeat.yml
WORKDIR /opt/service/classpath
COPY out/production/external-runtime-libraries external-libraries
COPY out/production/internal-runtime-libraries internal-libraries
COPY out/production/classes classes
ARG version=local
ENV VERSION $version
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    dpkg-reconfigure --frontend=noninteractive locales && \
    update-locale LANG=en_US.UTF-8
ENV LANG en_US.UTF-8
ENV USER_OWNED_FILE_TREES /var/log/updates-publisher /usr/share/filebeat
ARG commit
LABEL com.ssrn.commit="${commit}"