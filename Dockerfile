FROM hseeberger/scala-sbt
RUN mkdir /XSL-tester
COPY . /XSL-tester
RUN cd /XSL-tester
RUN ls
RUN apt-get update
RUN sbt
RUN run
