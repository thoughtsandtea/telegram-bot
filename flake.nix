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
        
        gradle2nixBuild = gradle2nix.builders.${system};
        project = gradle2nixBuild.gradleProject {
          name = "telegram-bot";
          src = ./.;
          lockFile = ./gradle.lock;
        };
        
        telegram-bot = project.buildGradlePackage {
          buildJdk = jdk;
          gradleJdk = jdk;
          
          # Run the installDist Gradle task
          gradleTask = "installDist";
          
          # Properly copy the distribution to the output
          installPhase = ''
            mkdir -p $out/bin
            mkdir -p $out/lib

            # First try the thoughtsntea-bot directory
            if [ -d build/install/thoughtsntea-bot ]; then
              cp -r build/install/thoughtsntea-bot/lib/* $out/lib/
              cp build/install/thoughtsntea-bot/bin/* $out/bin/
              chmod +x $out/bin/*
            # Or try the telegram-bot directory
            elif [ -d build/install/telegram-bot ]; then
              cp -r build/install/telegram-bot/lib/* $out/lib/
              cp build/install/telegram-bot/bin/* $out/bin/
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
        
        dockerImage = pkgs.dockerTools.buildImage {
          name = "telegram-bot";
          tag = "latest";
          
          copyToRoot = pkgs.buildEnv {
            name = "image-root";
            paths = [ telegram-bot pkgs.bashInteractive pkgs.coreutils ];
            pathsToLink = [ "/bin" "/lib" ];
          };
          
          config = {
            Cmd = [ "${telegram-bot}/bin/thoughtsntea-bot" ];
            Volumes = {
              "/thoughtsntea.json" = {};
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