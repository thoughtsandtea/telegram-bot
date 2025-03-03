{
  description = "TeaClub Telegram Bot";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    flake-compat = {
      url = "github:edolstra/flake-compat";
      flake = false;
    };
    buildGradleApplication = {
      url = "github:raphiz/buildGradleApplication";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils, flake-compat, buildGradleApplication, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [
            buildGradleApplication.overlays.default
          ];
        };
        jdk = pkgs.temurin-bin-23;
        
        # Use hardcoded version since VERSION is in .gitignore
        version = "0.0.1";
        appName = "thoughtsntea-bot";
        
        # Create application package
        app = pkgs.buildGradleApplication {
          pname = appName;
          version = version;
          src = ./.;
          
          jdk = jdk;
          
          # Standard maven repositories
          repositories = [
            "https://repo1.maven.org/maven2/"
            "https://plugins.gradle.org/m2/"
            "https://jitpack.io/"
          ];
          
          meta = with pkgs.lib; {
            description = "Telegram bot for organizing tea tasting sessions";
            homepage = "https://github.com/thoughtsandtea/telegram-bot";
            license = licenses.mit;
            platforms = platforms.all;
          };
        };
        
        # Create OCI container image
        dockerImage = pkgs.dockerTools.buildLayeredImage {
          name = appName;
          tag = version;
          
          contents = [
            app
            pkgs.coreutils
          ];
          
          config = {
            Cmd = [ "${app}/bin/thoughtsntea-bot" ];
            WorkingDir = "/data";
            Volumes = {
              "/data" = {};
            };
          };
        };
        
      in {
        packages = {
          default = app;
          dockerImage = dockerImage;
        };

        apps.default = flake-utils.lib.mkApp {
          drv = app;
          name = appName;
          exePath = "/bin/thoughtsntea-bot";
        };
        
        # Additional image formats
        packages.ociImage = dockerImage;
        packages.container = dockerImage;

        devShells.default = pkgs.mkShell {
          buildInputs = [
            jdk
            pkgs.gradle
            pkgs.skopeo  # For container image manipulation
          ];
          
          shellHook = ''
            echo "TeaClub Telegram Bot development environment"
            echo "JDK version: $(java -version 2>&1 | head -n 1)"
            echo "Gradle version: $(gradle --version | grep Gradle | head -n 1)"
            echo ""
            echo "Available commands:"
            echo "  nix build .#dockerImage - Build OCI container image"
            echo "  nix run .#default - Run the application directly"
            echo "  skopeo inspect docker-archive:./result - Inspect the container image"
          '';
        };
      }
    );
}