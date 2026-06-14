COMPOSE = docker compose -f docker/docker-compose.dev.yml --env-file .env.dev

# MySQL + 앱 전체 실행
up:
	$(COMPOSE) up -d

# MySQL만 실행 (로컬 bootRun 개발 시)
up-db:
	$(COMPOSE) up -d mysql

# 중지 및 컨테이너 제거
down:
	$(COMPOSE) down

# 컨테이너 상태 확인
ps:
	$(COMPOSE) ps

# 로그 확인 (전체)
logs:
	$(COMPOSE) logs -f

# 앱 로그만
logs-app:
	$(COMPOSE) logs -f app

# MySQL 로그만
logs-db:
	$(COMPOSE) logs -f mysql

# 도커 이미지 빌드
build:
	docker build -t lemonpay-backend:dev .

# 로컬 bootRun (MySQL 컨테이너에 붙어서 실행)
run:
	set -a && source .env.dev && set +a && SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# 테스트 실행 (dev 프로파일, MySQL 컨테이너 필요)
test:
	set -a && source .env.dev && set +a && SPRING_PROFILES_ACTIVE=dev ./gradlew test

# 테스트 실행 (local 프로파일, H2 인메모리 - MySQL 불필요)
test-local:
	./gradlew test -Dspring.profiles.active=local

.PHONY: up up-db down ps logs logs-app logs-db build run test test-local
