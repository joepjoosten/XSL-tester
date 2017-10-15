FROM hseeberger/scala-sbt
RUN mkdir /XSL-tester
COPY ./XSL-tester /XSL-tester
RUN cd /XSL-tester
RUN apt-get update
RUN sbt
RUN run