FROM rabbitmq:3.12.6-management-alpine
LABEL authors="valen"
# Copy the config files
COPY ./rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
COPY ./definitions.json /etc/rabbitmq/definitions.json
# Replace the startup script with a custom script to enable remote shutdown
COPY run.sh /usr/local/bin/docker-entrypoint.sh
# Make the script executable
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
# Expose the shutdown HTTP port
EXPOSE 5000