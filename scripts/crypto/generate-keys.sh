#!/bin/bash
# ============================================================
# generate-keys.sh
# Genera el par de claves RSA (pública/privada) para firmar
# y verificar JWTs con algoritmo RS256.
# Se ejecuta una sola vez durante el setup inicial.
# ============================================================

#
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Generate private key
openssl genrsa -out "${SCRIPT_DIR}/private.pem" 2048

# Generate public key
openssl rsa -in "${SCRIPT_DIR}/private.pem" -pubout -out "${SCRIPT_DIR}/public.pem"

echo ""
echo "-------------------------------------------------- GENERATED KEYS in ${SCRIPT_DIR} -- ADD to .gitignore --------------------------------------------------"

