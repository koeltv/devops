# Local GitLab setup

Inspired from [this tutorial](https://www.czerniga.it/2021/11/14/how-to-install-gitlab-using-docker-compose/).

- Run `docker-compose up -d` in this directory
- Wait for gitlab to build itself (can be pretty long)
- Login to [GitLab](http://localhost:8080) with `root` and the [root password](./secret.env)
- Disable sign-up via the popup and save (optional)
- Go to [http://localhost:8080/-/profile/personal_access_tokens](http://localhost:8080/-/profile/personal_access_tokens), `Add personal token` and create a token
- For IntelliJ, add the token in: `Settings > Version Control > Github/Gitlab`
- Go to [http://localhost:8080/projects/new#blank_project](http://localhost:8080/projects/new#blank_project) and create an empty project
- On the runner, run `gitlab-runner register --url "http://gitlab-ce" --clone-url "http://gitlab-ce"`
- Go to [http://localhost:8080/admin/runners](http://localhost:8080/admin/runners) and copy the registration token
- Finish configuration, my chosen default runner image: `ubuntu:23.10`
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
- Add a new remote via Git and point it to the blank project you created earlier

Everything is ready ! You can now push to the local GitLab and the pipeline should start automatically.

## SonarQube setup

Optionally, you can also configure SonarQube to integrate with the GitLab pipeline.
This is completely optional as the static analysis job will just be skipped if SonarQube is not setup.

- Make sure the necessary containers are running (normally launched along with GitLab)
- Go to [SonarQube](http://localhost:9000)
- Enter login and password (default: `admin`)
- Set a new password
- Click on `Import from GitLab > Setup`
  - Enter a name (not important here)
  - Create new [personal access token](http://localhost:8080/-/profile/personal_access_tokens) and add it
  - Paste the link to the GitLab API: [http://web/api/v4](http://web/api/v4)
  - Save the configuration
- When asked for a personal access token, enter the same as previously
- The repository should appear, select `Import` on the right
- Select `Use the global setting`, then `Create project` at the bottom
- Select `With GitLab CI`
- Follow the tutorial
  - Create a new project token in SonarQube (there is button to do so in the tutorial)
  - In GitLab go to [`Settings > CI/CD`](http://localhost:8080/admin/application_settings/ci_cd)
  - In `Variables` click on `Expand`
  - Add variable: Key = `SONAR_TOKEN`, Value = `<YOUR_SONARQUBE_PROJECT_TOKEN>`
  - Add variable: Key = `SONAR_HOST_URL`, Value = `http://sonarqube:9000`

Everything should now be working, you can trigger a new pipeline run to check it