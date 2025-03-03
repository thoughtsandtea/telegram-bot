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
        jdk = pkgs.jdk21;
        
        telegram-bot = gradle2nix.builders.${system}.buildGradlePackage {
          pname = "telegram-bot";
          version = "0.0.1";
          
          src = ./.;
          
          lockFile = ./gradle.lock;
          
          gradleInstallFlags = [ "installDist" ];
                    
          installPhase = ''
            mkdir -p $out
            cp -r build/install/thoughtsntea-bot/* $out/
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