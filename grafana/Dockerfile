FROM grafana/grafana:10.2.2
LABEL authors="valen"
# Copy the data file
COPY grafana.db /var/lib/grafana/grafana.db
# Make sure permissions are correct
USER root
RUN ["chmod", "a+w", "/var/lib/grafana/grafana.db"]
USER grafana