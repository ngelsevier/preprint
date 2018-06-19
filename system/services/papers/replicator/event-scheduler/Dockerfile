FROM 756659522569.dkr.ecr.us-east-1.amazonaws.com/ssrn/base:development
COPY supervisord.conf /etc/supervisor/conf.d/
RUN apt-get update && apt-get -y install python python-pip
COPY requirements.txt .
RUN pip install -r requirements.txt
WORKDIR /opt/scheduler
COPY scheduler.sh .
COPY job.py .