{
  description = "Telegram Bot";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    flake-compat = {
      url = "github:edolstra/flake-compat";
      flake = false;
    };
    gradle2nix = {
      url = "github:tadfisher/gradle2nix/v2";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, flake-utils, flake-compat, gradle2nix, ... }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
        jdk = pkgs.jdk23; # Using JDK 23 as specified in build.gradle.kts
        
        telegram-bot = gradle2nix.builders.${system}.buildGradlePackage {
          pname = "telegram-bot";
          version = "0.0.1";
          src = ./.;
          lockFile = ./gradle.lock;
          
          jdk = jdk;
          buildJdk = jdk;
          gradleFlags = [ 
            "-Dorg.gradle.java.home=${jdk}/lib/openjdk"
            "-Dorg.gradle.jvmargs=-Xmx2g"
          ];
          gradleBuildFlags = [ "installDist" ];
          
          # Properly copy the distribution to the output
          installPhase = ''
            mkdir -p $out/bin
            mkdir -p $out/lib

            # First try the thoughtsntea-bot directory
            if [ -d build/install/thoughtsntea-bot ]; then
              cp -r build/install/thoughtsntea-bot/lib/* $out/lib/
              cp -r build/install/thoughtsntea-bot/bin/* $out/bin/
              chmod +x $out/bin/*
            # Or try the telegram-bot directory
            elif [ -d build/install/telegram-bot ]; then
              cp -r build/install/telegram-bot/lib/* $out/lib/
              cp -r build/install/telegram-bot/bin/* $out/bin/
              chmod +x $out/bin/*
            else
              echo "No installation directory found"
              find build -type d
              exit 1
            fi
          '';
          
          meta = with pkgs.lib; {
            description = "Telegram bot for Tea Club";
            homepage = "https://github.com/teaguild/telegram-bot";
            license = licenses.mit;
            maintainers = [];
          };
        };
        
        # Create empty config file to ensure directory structure
        emptyConfig = pkgs.writeTextFile {
          name = "thoughtsntea.json";
          text = "{}";
        };
        
        dockerImage = pkgs.dockerTools.buildImage {
          name = "telegram-bot";
          tag = "latest";
          
          copyToRoot = pkgs.buildEnv {
            name = "image-root";
            paths = [ 
              telegram-bot 
              jdk 
              pkgs.bashInteractive 
              pkgs.coreutils     # Includes echo, printf, etc.
              pkgs.findutils     # Includes xargs
              pkgs.gnused        # Includes sed
              pkgs.gnugrep       # Includes grep 
              pkgs.which         # Includes which
              (pkgs.runCommand "app-dirs" {} ''
                mkdir -p $out/app
                # Don't create the config file directly, just the directory
                # so Docker can mount a volume there
              '')
            ];
            pathsToLink = [ "/bin" "/lib" "/app" ];
          };
          
          config = {
            Cmd = [ "${telegram-bot}/bin/thoughtsntea-bot" ];
            Env = [
              "JAVA_HOME=${jdk}/lib/openjdk"
              "PATH=${jdk}/lib/openjdk/bin:/bin"
            ];
            WorkingDir = "/app";
            # Match the original Docker volume configuration
            Volumes = {
              "/app/thoughtsntea.json" = {};
            };
          };
        };
      in {
        packages = {
          default = telegram-bot;
          inherit dockerImage;
        };
        
        apps.default = flake-utils.lib.mkApp {
          drv = telegram-bot;
          exePath = "/bin/thoughtsntea-bot";
        };
        
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk
            gradle
          ];
        };
      }
    );
}