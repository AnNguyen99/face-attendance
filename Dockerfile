FROM anapsix/alpine-java
VOLUME uploads
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} face-attendance.jar
COPY JKB.png JKB.png
COPY tuan.png tuan.png
ADD Data Data
ADD Libs Libs
ENV PATH=/Libs:/Data:$PATH
CMD env
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "face-attendance.jar"]
