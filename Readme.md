# Bleep ZIO-Http Sample

This is a sample project based on ZIO and ZIO-HTTP using Scala [Bleep](https://bleep.build/docs/) build tool which uses a simple, data-only build file written in YAML (`bleep.yaml`) that has no logic or code and is blazing fast.

## Pre-Reqs

I recommend [using Coursier](https://get-coursier.io/docs/cli-installation#native-launcher) to manage tooling install.

On Windows, download and install using the [Windows Installer](https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-win32.zip).

```sh
# For Linux
curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
# For MacOS
curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-apple-darwin.gz | gzip -d > cs

chmod +x cs
./cs setup

# Install Bleep build tool:
cs install --channel https://raw.githubusercontent.com/oyvindberg/bleep/master/coursier-channel.json bleep
```

## Running and Testing

Run the main server with:

```sh
bleep run httpserver
```

Test the project with:

```sh
bleep test
```
