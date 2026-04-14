FROM gradle:9.0.0-jdk21

WORKDIR /workspace

ENV GRADLE_USER_HOME=/workspace/.gradle-user-home

CMD ["gradle", "--version"]
