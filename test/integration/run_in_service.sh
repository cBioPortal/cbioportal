run_in_service() {
    service=$1
    shift
    # COMPOSE_FILE is set by the integration-test workflow (e.g. to add docker-compose-core.yml)
    COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml:$PORTAL_SOURCE_DIR/test/integration/docker-compose-localbuild.yml}" \
    docker compose \
        run --rm \
        "$service" bash -c "$@"
    return $?  # return the exit code of the last command
}