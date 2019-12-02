FROM openjdk:11
WORKDIR /app
EXPOSE 8080

COPY Contents/client.ncconf client.ncconf
COPY Contents/license.properties license.properties
COPY Contents/ncachejavaclient-0.0.1-SNAPSHOT.jar ncachejavaclient-0.0.1-SNAPSHOT.jar
COPY Contents/sigar-amd64-winnt.dll C:/openjdk-11/bin/sigar-amd64-winnt.dll

ENV NCacheDiscoveryURL='dummyincontainer'

ENTRYPOINT ["java", "-jar","ncachejavaclient-0.0.1-SNAPSHOT.jar"]
