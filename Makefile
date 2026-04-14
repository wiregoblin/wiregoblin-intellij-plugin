SHELL := /bin/sh

DOCKER_IMAGE ?= wiregoblin-intellij-plugin-build
DOCKERFILE ?= Dockerfile
GRADLE_USER_HOME ?= /workspace/.gradle-user-home
DOCKER_RUN = docker run --rm \
	-v "$(CURDIR)":/workspace \
	-w /workspace \
	-e GRADLE_USER_HOME=$(GRADLE_USER_HOME) \
	$(DOCKER_IMAGE)

.PHONY: help docker-image wrapper generate build dist release test verify clean run-ide-local

help:
	@printf "%s\n" \
		"make docker-image   Build pinned Gradle/JDK image" \
		"make wrapper        Generate Gradle wrapper via Docker" \
		"make generate       Generate embedded JSON schema via Docker" \
		"make build          Compile and verify the plugin via Docker" \
		"make dist           Build installable plugin ZIP via Docker" \
		"make release        Alias for dist; produce installable release ZIP" \
		"make test           Run tests via Docker" \
		"make verify         Run verification tasks via Docker" \
		"make clean          Clean build outputs via Docker" \
		"make run-ide-local  Run sandbox IDE locally via Gradle wrapper"

docker-image:
	docker build -t $(DOCKER_IMAGE) -f $(DOCKERFILE) .

wrapper: docker-image
	$(DOCKER_RUN) gradle wrapper

generate: docker-image
	$(DOCKER_RUN) gradle generateWireGoblinSchema

build: docker-image
	$(DOCKER_RUN) gradle build

dist: docker-image
	$(DOCKER_RUN) gradle buildPlugin

release: dist

test: docker-image
	$(DOCKER_RUN) gradle test

verify: docker-image
	$(DOCKER_RUN) gradle check

clean: docker-image
	$(DOCKER_RUN) gradle clean

run-ide-local:
	@if [ ! -x ./gradlew ]; then echo "Missing ./gradlew. Run 'make wrapper' first."; exit 1; fi
	./gradlew runIde
