FROM schemathesis/schemathesis:v3.21.0-buster
LABEL authors="valen"
# Copy OpenAPI specification file(s)
COPY apis /apis
# Verify conformity to the spec. See documentation here: https://schemathesis.readthedocs.io/
ENTRYPOINT ["schemathesis", "run", "--checks", "all", "--base-url", "http://gateway", "/apis/openapi.yml"]