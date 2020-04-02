FROM python:3.8-alpine

RUN python3 -m pip install robotframework
RUN pip install -U requests
RUN pip install -U robotframework-requests
ADD atest/ /opt/robotframework/
ADD test.sh /opt/robotframework/

CMD ["sh", "/opt/robotframework/test.sh"]