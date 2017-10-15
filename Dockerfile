FROM hseeberger/scala-sbt
RUN mkdir /XSL-tester
COPY . .
RUN apt-get update
RUN sbt
RUN run
