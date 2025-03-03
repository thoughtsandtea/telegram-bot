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
      in {
        packages.default = pkgs.stdenv.mkDerivation {
          pname = "thoughtsntea-bot";
          version = builtins.readFile ./VERSION;
          
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

        apps.default = flake-utils.lib.mkApp {
          drv = self.packages.${system}.default;
          name = "thoughtsntea-bot";
          exePath = "/bin/thoughtsntea-bot";
        };

        devShells.default = pkgs.mkShell {
          buildInputs = [
            jdk
            pkgs.gradle
          ];
          
          shellHook = ''
            echo "TeaClub Telegram Bot development environment"
            echo "JDK version: $(java -version 2>&1 | head -n 1)"
            echo "Gradle version: $(gradle --version | grep Gradle | head -n 1)"
          '';
        };
      }
    );
}