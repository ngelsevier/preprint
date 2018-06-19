ARG parent_image_version=development
FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/java:${parent_image_version}
RUN apt-get update && \
    apt-get install -y wget apt-transport-https && \
    wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add - && \
    echo "deb https://artifacts.elastic.co/packages/5.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-5.x.list && \
    apt-get update && \
    apt-get install -y logstash && \
    apt-get remove -y wget apt-transport-https && \
    apt-get -y autoremove && \
    apt-get clean
RUN /usr/share/logstash/bin/logstash-plugin install logstash-output-amazon_es
COPY supervisord.conf /etc/supervisor/conf.d/
COPY logstash.conf /etc/logstash/conf.d/
COPY log4j2.properties /etc/logstash
ARG version=local
ENV VERSION $version
ENV USER_OWNED_FILE_TREES /var/lib/logstash
ARG commit
LABEL com.ssrn.commit="${commit}"