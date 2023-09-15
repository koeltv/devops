# Results

Langages used are [Kotlin](https://kotlinlang.org/) and [Rust](https://www.rust-lang.org/)

## Output of `docker container ls`
```
CONTAINER ID   IMAGE             COMMAND                  CREATED              STATUS         PORTS                    NAMES
3ee54a43f774   rnvako-service2   "java -jar service.j…"   About a minute ago   Up 3 seconds   0.0.0.0:8000->8000/tcp   rnvako_service2
a49a84a4e5d6   rnvako-service1   "service1"               7 minutes ago        Up 3 seconds                            rnvako_service1
```

## Output of `docker network ls`
```
NETWORK ID     NAME                       DRIVER    SCOPE
79e447432076   bridge                     bridge    local
da594a2fe7fc   host                       host      local
37bfab685630   none                       null      local
13be1bb0dca1   rnvako_default             bridge    local
```