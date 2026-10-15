#!/bin/sh
set -eu

: "${CRERAL_DATABASE_HOST:?Informe o host MySQL no ambiente do Render.}"
: "${CRERAL_DATABASE_PORT:?Informe a porta MySQL.}"
: "${CRERAL_DATABASE_NAME:?Informe o nome da base nova.}"
: "${CRERAL_DATABASE_USERNAME:?Informe o usuario MySQL.}"
: "${CRERAL_DATABASE_PASSWORD:?Informe a senha MySQL no ambiente protegido.}"
case "$CRERAL_DATABASE_HOST" in *[!a-zA-Z0-9.-]*) echo 'Host MySQL invalido.' >&2; exit 1;; esac
case "$CRERAL_DATABASE_PORT" in *[!0-9]*) echo 'Porta MySQL invalida.' >&2; exit 1;; esac
case "$CRERAL_DATABASE_NAME" in *[!a-zA-Z0-9_]*) echo 'Nome da base invalido.' >&2; exit 1;; esac

ca_file="${CRERAL_CA_FILE:-/etc/secrets/aiven-ca.pem}"
[ -r "$ca_file" ] || { echo 'Certificado CA do MySQL ausente ou ilegivel.' >&2; exit 1; }
runtime_dir="$(mktemp -d /tmp/creral-internet.XXXXXX)"
store="$runtime_dir/aiven.p12"
keytool -importcert -noprompt -alias aiven -file "$ca_file" -keystore "$store" \
  -storetype PKCS12 -storepass changeit >/dev/null 2>&1 || {
    echo 'Nao foi possivel preparar a confianca TLS do MySQL.' >&2; exit 1;
  }
# A senha changeit protege somente a CA publica, sem chave privada.
# A senha da conta MySQL permanece separada, exclusivamente no ambiente.
export CRERAL_DATABASE_URL="jdbc:mysql://$CRERAL_DATABASE_HOST:$CRERAL_DATABASE_PORT/$CRERAL_DATABASE_NAME?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=file:$store&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&fallbackToSystemTrustStore=false"

if [ "${1:-}" = preparar ]; then
  shift
  exec java -cp '/app/classes:/app/lib/*' com.patp.sistema.PrepararBancoInternet "$@"
fi
[ "$#" -eq 0 ] || { echo 'Comando de inicio invalido.' >&2; exit 1; }
: "${CRERAL_FRONTEND_ORIGIN:?Informe a origem HTTPS da interface Vercel.}"
exec java -cp '/app/classes:/app/lib/*' com.patp.sistema.SistemaApplication \
  --spring.config.location=classpath:/application-internet.properties \
  --spring.profiles.active=internet --creral.internet.enabled=true
