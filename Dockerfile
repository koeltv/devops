FROM ubuntu:23.10
# Update packages and install necessary ones, empty cache when done
RUN apt-get update && apt-get install -y --no-install-recommends openssh-server python3 sudo net-tools && rm -rf /var/lib/apt/lists/*
# Create a sudo user for ssh
RUN useradd -m -s /bin/bash -G sudo sshuser
RUN echo 'sshuser ALL=(ALL:ALL) NOPASSWD:ALL' > /etc/sudoers.d/sshuser
# Add ssh public key with necessary permissions
COPY id_rsa.pub /home/sshuser/.ssh/authorized_keys
RUN chown -R sshuser:sshuser /home/sshuser/.ssh
RUN chmod 600 /home/sshuser/.ssh/authorized_keys
# Expose SSH port
EXPOSE 22
# Start SSH and wait
ENTRYPOINT service ssh start && sleep infinity