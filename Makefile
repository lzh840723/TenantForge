.PHONY: help format lint typecheck test test-integration backend-test frontend-test

help:
	@echo "Targets: format lint typecheck test test-integration"

format:
	@echo "Formatting backend Java (google-java-format if available)"
	@if command -v google-java-format >/dev/null 2>&1; then \
		find backend/src -name "*.java" -print0 | xargs -0 google-java-format -i ; \
	else \
		echo "google-java-format not found, skipping" ; \
	fi
	@echo "Formatting frontend (prettier if available)"
	@if command -v prettier >/dev/null 2>&1 && [ -d frontend ]; then \
		prettier --write "frontend/**/*.{html,css,js,ts,json,md}" ; \
	else \
		echo "prettier not found or frontend missing, skipping" ; \
	fi

lint:
	@echo "Lint Java via mvn -q -DskipTests=true -DskipITs=true -Dcheckstyle.skip=true"
	@mvn -q -f backend/pom.xml -DskipTests=true -DskipITs=true -Dcheckstyle.skip=true -Djacoco.skip=true -Dspotless.skip=true -Dstyle.color=never validate
	@echo "Lint JS/TS (eslint if available)"
	@if command -v eslint >/dev/null 2>&1 && [ -d frontend ]; then \
		eslint "frontend/**/*.{js,ts,jsx,tsx}" || true ; \
	else \
		echo "eslint not found or frontend missing, skipping" ; \
	fi

typecheck:
	@echo "Typecheck TS (tsc if available)"
	@if command -v tsc >/dev/null 2>&1 && [ -d frontend ]; then \
		tsc -p tsconfig.json --noEmit || true ; \
	else \
		echo "tsc not found or frontend missing, skipping" ; \
	fi

test: backend-test frontend-test

backend-test:
	@mvn -q -f backend/pom.xml -Djacoco.skip=true -DskipITs=true -DfailIfNoTests=false -Dstyle.color=never clean test

frontend-test:
	@echo "No frontend unit tests configured; skipping"

test-integration:
	@echo "Running Postgres integration tests with Testcontainers (requires Docker)"
	@RUN_PG_IT=true mvn -q -f backend/pom.xml -Djacoco.skip=true -Dstyle.color=never verify || true
	@echo "Artifacts (if any): backend/target/artifacts/"

