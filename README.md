# How to run

1. Copy the private SSH key `id_rsa` to `~/.ssh/` on the host
2. Setup Ansible (if not already done)
    1. `sudo apt install python3-pip`
    2. `python3 -m pip install --user ansible`
3. Build the docker image (for example with `docker build -t ssh-image .`)
4. Start the first docker image, for example:
   1. In a linux environment: `docker run -d ssh-image`
   2. In a windows-WSL environment: `docker run -p 2222:22 -d ssh-image` (we need to expose the port to access the container)
5. Get the IP:
   1. In a linux environment: `docker exec <CONTAINER> ifconfig` or `docker inspect`
   2. In a windows-WSL environment: we will use `127.0.0.X` (`127.0.0.1`, `127.0.0.2`, ...)
6. Add the IP to `/etc/ansible/hosts` under `[webservers]` like so:
   ```text
   [webservers]
   127.0.0.X:2222
   ```
7. Run the ansible playbook twice using `ansible-playbook playbook.yml`
8. Start the second docker image
9. Add the IP to `/etc/ansible/hosts` after the previous one
10. Re-run the ansible playbook twice

# Ansible outputs

Since I am using Windows with WSL, I can't directly use the containers IPs, 
so I am mapping the containers to a port on the host and using localhost adresses (`127.0.0.X`).

### O1
```text
PLAY [webservers] ********************************************************************************************************************************************************************

TASK [Gathering Facts] ***************************************************************************************************************************************************************
ok: [127.0.0.1]

TASK [ensure git is at the latest version] *******************************************************************************************************************************************
changed: [127.0.0.1]

TASK [query the uptime] **************************************************************************************************************************************************************
changed: [127.0.0.1]

TASK [display uptime] ****************************************************************************************************************************************************************
ok: [127.0.0.1] => {
    "uptime.stdout": " 21:29:29 up  2:22,  0 user,  load average: 0.37, 0.12, 0.08"
}

PLAY RECAP ***************************************************************************************************************************************************************************
127.0.0.1                  : ok=4    changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

### O2
```text
PLAY [webservers] ********************************************************************************************************************************************************************

TASK [Gathering Facts] ***************************************************************************************************************************************************************
ok: [127.0.0.1]

TASK [ensure git is at the latest version] *******************************************************************************************************************************************
ok: [127.0.0.1]

TASK [query the uptime] **************************************************************************************************************************************************************
changed: [127.0.0.1]

TASK [display uptime] ****************************************************************************************************************************************************************
ok: [127.0.0.1] => {
    "uptime.stdout": " 21:29:55 up  2:23,  0 user,  load average: 0.33, 0.13, 0.08"
}

PLAY RECAP ***************************************************************************************************************************************************************************
127.0.0.1                  : ok=4    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

### O3
```text
PLAY [webservers] ********************************************************************************************************************************************************************

TASK [Gathering Facts] ***************************************************************************************************************************************************************
ok: [127.0.0.1]
ok: [127.0.0.2]

TASK [ensure git is at the latest version] *******************************************************************************************************************************************
ok: [127.0.0.1]
changed: [127.0.0.2]

TASK [query the uptime] **************************************************************************************************************************************************************
changed: [127.0.0.2]
changed: [127.0.0.1]

TASK [display uptime] ****************************************************************************************************************************************************************
ok: [127.0.0.1] => {
    "uptime.stdout": " 21:31:52 up  2:25,  0 user,  load average: 0.39, 0.16, 0.10"
}
ok: [127.0.0.2] => {
    "uptime.stdout": " 21:31:52 up  2:25,  0 user,  load average: 0.39, 0.16, 0.10"
}

PLAY RECAP ***************************************************************************************************************************************************************************
127.0.0.1                  : ok=4    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
127.0.0.2                  : ok=4    changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

### O4
```text
PLAY [webservers] ********************************************************************************************************************************************************************

TASK [Gathering Facts] ***************************************************************************************************************************************************************
ok: [127.0.0.2]
ok: [127.0.0.1]

TASK [ensure git is at the latest version] *******************************************************************************************************************************************
ok: [127.0.0.1]
ok: [127.0.0.2]

TASK [query the uptime] **************************************************************************************************************************************************************
changed: [127.0.0.2]
changed: [127.0.0.1]

TASK [display uptime] ****************************************************************************************************************************************************************
ok: [127.0.0.1] => {
    "uptime.stdout": " 21:32:20 up  2:25,  0 user,  load average: 0.46, 0.20, 0.11"
}
ok: [127.0.0.2] => {
    "uptime.stdout": " 21:32:20 up  2:25,  0 user,  load average: 0.46, 0.20, 0.11"
}

PLAY RECAP ***************************************************************************************************************************************************************************
127.0.0.1                  : ok=4    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0   
127.0.0.2                  : ok=4    changed=1    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

# Comments

The uptime command add an output offset by -2 hours, this is because it defaulted to GMT time.

The ansible setup and playbook creation was pretty easy since it is well documented. 
The only issue I had was setting up the container so that ansible could communicate with them, because of the limitation of Windows with WSL.