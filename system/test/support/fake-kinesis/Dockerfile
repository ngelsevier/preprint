FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/java:development
RUN apt-get update && \
    apt-get -y install nodejs npm awscli curl
RUN ln -s /usr/bin/nodejs /usr/bin/node
RUN npm install -g kinesalite
COPY configure-kinesis.sh /usr/local/bin
COPY supervisord.conf /etc/supervisor/conf.d/
WORKDIR /opt/dynamodb
RUN curl -fsL -o dynamodb_local_latest.tar.gz https://s3.eu-central-1.amazonaws.com/dynamodb-local-frankfurt/dynamodb_local_latest.tar.gz && \
    tar xvf dynamodb_local_latest.tar.gz && \
    rm  dynamodb_local_latest.tar.gz