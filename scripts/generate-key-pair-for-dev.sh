#!/usr/bin/env bash
openssl req -newkey rsa:2048 -subj "/O=Digital Identity/CN=ipv-signing" -new -nodes -x509 -days 3650 -keyout signing-key.pem -out signing-cert.pem