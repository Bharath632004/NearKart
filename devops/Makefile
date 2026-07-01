# ═══════════════════════════════════════════════════════════════════
#  NearKart – DevOps Makefile
#  Usage: make <target>  (run from /devops folder)
# ═══════════════════════════════════════════════════════════════════

COMPOSE = docker-compose -f docker-compose.yml

.PHONY: help up down build restart logs ps clean prune

help:  ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

up: ## Start all services (detached)
	$(COMPOSE) up -d

build: ## Build all Docker images
	$(COMPOSE) build --no-cache

down: ## Stop and remove containers
	$(COMPOSE) down

restart: down up ## Restart all services

logs: ## Tail logs for all services
	$(COMPOSE) logs -f

logs-%: ## Tail logs for a specific service e.g. make logs-auth-service
	$(COMPOSE) logs -f $*

ps: ## Show running containers
	$(COMPOSE) ps

clean: down ## Stop containers and remove volumes
	$(COMPOSE) down -v --remove-orphans

prune: clean ## Full Docker system prune (WARNING: removes all unused images)
	docker system prune -af --volumes

db-shell: ## Open psql shell in postgres container
	docker exec -it nearkart-postgres psql -U nearkart_user -d nearkart

redis-cli: ## Open redis-cli
	docker exec -it nearkart-redis redis-cli
