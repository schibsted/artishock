![](src/main/logo/artishock.svg)
# Artishock
A tool to investigate Dependency Confusion in Artifactory.

## Install
Binaries for Linux, Mac and Windows can be found under [releases](https://github.com/schibsted/artishock/releases) (Windows is not tested).

Unzip `artishock-{linux,mac,win}.zip`, then run `artishock-{linux,mac,win}/bin/artishock`.

**For macOs**: `bin/artishock` and `bin/java` are not signed so they must be allowed to run.

## Configure
Create `~/.artishock/artishock.config` with the following
```
{
  "artifactoryUrl": "https://example.com/artifactory/",
  "artifactoryUsername": "email@example.com",
  "artifactoryPassword": ""
}
```

You can also set these as environment variables:
```
export ARTISHOCK_ARTIFACTORY_URL=
export ARTISHOCK_ARTIFACTORY_USERNAME=
export ARTISHOCK_ARTIFACTORY_PASSWORD=
```

## Run

**Please note that some Artishock commands will look up your internal package names upstream.** These require the `--query-upstream` flag. If you do not want to disclose your internal names don't use those commands.

Some requests will be cached to `~/.artishock/cache/`. Each request is cached for 7 days. The cache directory can be deleted to clear the cache.
```
artishock
artishock repo-ls --help
artishock repo-ls --json
```

## Examples

### NPM
```
artishock repo-ls --package-system npm
artishock exclude-candidates --package-system npm --local npm-local
artishock not-claimed --package-system npm --local npm-local --query-upstream
artishock cached --package-system npm --local npm-local --remote npm-remote
artishock inferred-exclude --package-system npm --local npm-local --remote npm-remote --query-upstream
artishock package-stats --package-system npm --repo npm-local --package @example/package
```

### PyPi
```
artishock repo-ls --package-system pypi
artishock exclude-candidates --package-system pypi --local pypi-local
artishock not-claimed --package-system pypi --local pypi-local --query-upstream
artishock cached --package-system pypi --local pypi-local --remote pypi-remote
artishock inferred-exclude --package-system pypi --local pypi-local --remote pypi-remote --query-upstream
```

### Maven
This is a slow command that iterates over the whole repo to gather download stats
```
artishock repo-stats --package-system maven --repo small-remote-cache
```

## Debugging
Use `--verbose` for verbose output and `--stacktrace` to get the full stack trace rather than just the message.

## Developer notes
*Prerequisite: [OpenJDK 15](https://adoptopenjdk.net/?variant=openjdk15&jvmVariant=hotspot)*

Generate runtime images `build/image/artishock-{linux,mac,win}/`
```
./gradlew runtime
```

On Linux run the program with
```
build/image/artishock-linux/bin/artishock
```

Make release files `/build/artishoc-{linux,mac,win}.zip`
```
./gradlew runtimeZip
```
