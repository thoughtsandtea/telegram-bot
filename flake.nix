{
  description = "TeaClub Telegram Bot";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    flake-compat = {
      url = "github:edolstra/flake-compat";
      flake = false;
    };
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jdk = pkgs.temurin-bin-23;
        
        version = builtins.readFile ./VERSION;
        appName = "thoughtsntea-bot";
        
        # Create application package
        app = pkgs.stdenv.mkDerivation {
          pname = appName;
          version = version;
          
          src = ./.;
          
          nativeBuildInputs = [
            jdk
          ];
          
          buildPhase = ''
            export GRADLE_USER_HOME=$(mktemp -d)
            ./gradlew installDist
          '';
          
          installPhase = ''
            mkdir -p $out
            cp -r build/install/thoughtsntea-bot/* $out/
          '';
          
          meta = {
            description = "Telegram bot for organizing tea tasting sessions";
            homepage = "https://github.com/thoughtsandtea/telegram-bot";
            license = pkgs.lib.licenses.mit;
            platforms = pkgs.lib.platforms.all;
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