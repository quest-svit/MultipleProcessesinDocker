# syntax=docker/dockerfile:1
FROM openjdk:8
COPY customer-service/target/customer-service-*.jar /app/
COPY lucky-winner/target/lucky-winner-*.jar /app/
COPY script.sh /app
RUN chmod +x /app/script.sh

EXPOSE 8081
EXPOSE 8082

ENTRYPOINT ["sh" , "/app/script.sh"]

