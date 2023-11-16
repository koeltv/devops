# Local GitLab setup

Inspired from [this tutorial](https://www.czerniga.it/2021/11/14/how-to-install-gitlab-using-docker-compose/).

- Run `docker-compose up -d` in this directory
- Wait for gitlab to build itself (can be pretty long)
- Login with `root` and the root password
- Disable sign-up and save (optional)
- Go to [http://localhost:8080/-/profile/personal_access_tokens](http://localhost:8080/-/profile/personal_access_tokens), `Add personal token` and create a token
- For IntelliJ, add the token in: `Settings > Version Control > Github/Gitlab`
- Go to [http://localhost:8080/projects/new#blank_project](http://localhost:8080/projects/new#blank_project) and create an empty project
- On the runner, run `gitlab-runner register --url "http://gitlab-ce" --clone-url "http://gitlab-ce"`
- Go to [http://localhost:8080/admin/runners](http://localhost:8080/admin/runners) and copy the registration token
- Finnish configuration, my chosen default runner image: `ubuntu:23.10`
- In the [config.toml](./gitlab-runner/config.toml) change the end like so:
```toml
    [[runners]]
    name = "docker-runner"
    url = "http://gitlab-ce"
    ...
    [runners.docker]
        ...
        volumes = ["/var/run/docker.sock:/var/run/docker.sock", "/cache"]
        network_mode = "gitlab-network"
```

Everything is ready ! You can now push to GitLab and the pipeline should start automatically.