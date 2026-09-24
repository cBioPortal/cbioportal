#!/usr/bin/env bash
# Prepare the external Compose fixture used by CircleCI.
#
# cbioportal-test's helper parses DOCKER_IMAGE_CBIOPORTAL back out of the
# generated .env file.  That is fragile in fork jobs (and its init scripts do
# not stop when extraction fails, so Docker creates directory bind mounts for
# missing files).  Keep the test harness on the exact image already inspected
# by the job and make every extracted input an explicit file.
set -euo pipefail

: "${DOCKER_IMAGE_CBIOPORTAL:?DOCKER_IMAGE_CBIOPORTAL must be set before starting the test stack}"

ROOT_DIR="${CIRCLE_WORKING_DIRECTORY:-/tmp/repos}"
TEST_DIR="$ROOT_DIR/cbioportal-test"
COMPOSE_DIR="${CBIOPORTAL_DOCKER_COMPOSE_SOURCE:-$ROOT_DIR/cbioportal-docker-compose}"

if [[ ! -d "$COMPOSE_DIR/.git" ]]; then
  git clone --depth 1 https://github.com/cbioportal/cbioportal-docker-compose.git "$COMPOSE_DIR"
fi

python3 - "$COMPOSE_DIR/config/init.sh" "$COMPOSE_DIR/data/init.sh" "$COMPOSE_DIR/dev/keycloak/keycloak.yml" "$TEST_DIR/scripts/docker-compose.sh" "$COMPOSE_DIR" <<'PY'
from pathlib import Path
import sys

config_init, data_init, keycloak_compose, compose_helper, compose_dir = map(Path, sys.argv[1:])

config_text = config_init.read_text()
if not config_text.startswith("#!/usr/bin/env bash\nset -euo pipefail\n"):
    config_text = config_text.replace("#!/usr/bin/env bash\n", "#!/usr/bin/env bash\nset -euo pipefail\n", 1)
config_text = config_text.replace(
    "VERSION=$(grep DOCKER_IMAGE_CBIOPORTAL ../.env | tail -n 1 | cut -d '=' -f 2-)",
    'VERSION="${DOCKER_IMAGE_CBIOPORTAL:?DOCKER_IMAGE_CBIOPORTAL must be set}"',
)
config_text = config_text.replace(
    "--env-file ../.env.temp $VERSION bin/sh",
    '--env-file ../.env.temp "$VERSION" /bin/sh',
)
config_init.write_text(config_text)

data_text = data_init.read_text()
data_text = data_text.replace(
    "VERSION=$(grep DOCKER_IMAGE_CBIOPORTAL ../.env | tail -n 1 | cut -d '=' -f 2-)",
    'VERSION="${DOCKER_IMAGE_CBIOPORTAL:?DOCKER_IMAGE_CBIOPORTAL must be set}"',
)
data_text = data_text.replace(
    'VERSION=$(grep DOCKER_IMAGE_CBIOPORTAL "${SCRIPT_DIR}/../.env" | tail -n 1 | cut -d \'=\' -f 2-)',
    'VERSION="${DOCKER_IMAGE_CBIOPORTAL:?DOCKER_IMAGE_CBIOPORTAL must be set}"',
)
data_text = data_text.replace("docker create $VERSION", 'docker create "$VERSION"')
if 'for required in schema.sql seed.sql.gz populate_derived_tables.sql' not in data_text:
    data_text += (
        '\nfor required in schema.sql seed.sql.gz populate_derived_tables.sql; do\n'
        '  test -s "${SCRIPT_DIR}/${required}"\n'
        'done\n'
    )
data_init.write_text(data_text)

keycloak_text = keycloak_compose.read_text()
keycloak_text = keycloak_text.replace(
    "--spring.config.location=cbioportal-webapp/application.properties",
    "--spring.config.location=/cbioportal-webapp/application.properties",
)
keycloak_compose.write_text(keycloak_text)

helper_text = compose_helper.read_text()
repo_line = 'REPO_URL="file://%s"' % compose_dir
lines = helper_text.splitlines()
for i, line in enumerate(lines):
    if line.startswith('REPO_URL='):
        lines[i] = repo_line
        break
else:
    raise SystemExit('cbioportal-test helper no longer declares REPO_URL')
compose_helper.write_text('\n'.join(lines) + '\n')
PY

chmod +x "$COMPOSE_DIR/config/init.sh" "$COMPOSE_DIR/data/init.sh" "$TEST_DIR/scripts/docker-compose.sh"

# docker-compose.sh checks out DOCKER_COMPOSE_REF (master in the Circle jobs)
# after cloning.  Commit the two local harness edits on that temporary clone
# so the checkout cannot silently discard them.
if ! git -C "$COMPOSE_DIR" diff --quiet -- config/init.sh data/init.sh dev/keycloak/keycloak.yml; then
  git -C "$COMPOSE_DIR" config user.email "ci@cbioportal.org"
  git -C "$COMPOSE_DIR" config user.name "cBioPortal CI"
  git -C "$COMPOSE_DIR" add config/init.sh data/init.sh dev/keycloak/keycloak.yml
  git -C "$COMPOSE_DIR" commit -m "ci: make backend test fixture startup deterministic" >/dev/null
fi
