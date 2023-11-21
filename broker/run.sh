#!/bin/bash

# Start a simple HTTP server to handle shutdown requests (any HTTP request)
nc -l -p 5000 -e /bin/sh -c 'printf "HTTP/1.1 200 OK\r\n\r\n"; sleep 5; rabbitmqctl stop;' &

# Run RabbitMQ server
rabbitmq-server