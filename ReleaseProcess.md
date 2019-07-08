# Release process for OrderlyWeb

1. Get the version of the OrderlyWeb repo that you want to release locally (typically the latest master branch).  
1. Tag and build the release log by running 
   `./scripts/release/make-release.py`
   This script will warn you if any tickets are merged in that are not "Ready 
   to Deploy". It will prompt you to push the tag to git and to tag and push Docker images by running the tag_images 
   script:
    1. Push the tag to git and check the build has passed in Teamcity
    1. Run `./scripts/release/tag_images.py tag latest` to tag and push the release to the private registry. 
1. Deploy to the support machine(s) and test (see below for deployment instructions). 
1. When you are ready to publish the release, run `./scripts/release/tag_images.py publish latest` to push the 
   version-tagged images to the public registry, and also push a 'release' tag, which will always tag the latest 
   published release.
1. Deploy to production (see below).
1. The script will have updated relevant YouTrack tickets' `Fixed in build` field. Update these to the 'Deployed' status.

## Deploying to UAT / Science / Production

1. Connect to the machine:
    - For uat or science, connect as the vagrant user: `ssh vagrant@support.montagu` (or `ssh support.montagu` and then 
       `sudo su vagrant && cd`). Then run `./uat.sh` or `./science.sh` which will give you a shell inside the virtual 
       machine.
    - For production, `ssh -p 10022 montagu@production.montagu.dide.ic.ac.uk`   
1. Navigate to `montagu-orderly-web` and follow the instructions 
   [here](https://github.com/vimc/montagu-orderly-web/blob/master/README.md) to deploy the release version.