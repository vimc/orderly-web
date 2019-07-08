#!/usr/bin/env python3
"""Tags images used in a particular release and pushes the tags into
our local docker registry at docker.montagu.dide.ic.ac.uk:5000.  Use
tag 'latest' to select the most recent conforming git tag (this will
not set things to be the docker 'latest' tag though).  If run with the
"publish" option it will also publish images to
https://hub.docker.com/u/vimc and publish a 'release' tag there which will
always give the latest released public version.

Usage:
  tag_images.py tag [--publish] <version>
  tag_images.py tag publish <version>

"""
import docker
import docopt
import subprocess
from release_tag import get_latest_release_tag, validate_release_tag

# List of container images that we will tag
containers = [
    "orderly-web",
    "orderlyweb-migrate",
    "orderly-web-user-cli",
    "orderly-web-css-generator"
]

registry_local = "docker.montagu.dide.ic.ac.uk:5000"
registry_hub = "vimc"
current_release_tag = "release"


class DockerTag:
    def __init__(self, registry, name, version=None):
        self.registry = registry
        self.name = name
        self.version = version

    def __str__(self):
        if self.version:
            return "{}/{}:{}".format(self.registry, self.name, self.version)
        else:
            return "{}/{}".format(self.registry, self.name)

    @property
    def repository(self):
        return "{}/{}".format(self.registry, self.name)

    @classmethod
    def parse(cls, raw):
        registry, parts = raw.split("/")
        name, version = parts.split(":")
        return DockerTag(registry, name, version)


def get_version_sha(version):
    long_sha =  subprocess.run(["git", "rev-list", "-n", "1", version],
               stdout=subprocess.PIPE, check=True, universal_newlines=True).stdout.strip()
    return long_sha[:7]


def set_image_tag(name, version, sha):
    d = docker.client.from_env()
    img = d.images.pull(str(DockerTag(registry_local, name, sha)))
    tag_and_push(img, registry_local, name, version)


def set_image_tags(version):
    sha = get_version_sha(version)
    print("Setting image tags")
    for name in containers:
        print("  - " + name)
        set_image_tag(name, version, sha)


def publish_images(version):
    d = docker.client.from_env()
    print("Pushing release to docker hub")
    for name in containers:
        img = d.images.get(str(DockerTag(registry_local, name, version)))
        publish_image(img, name)


def publish_image(img, name):
    tags = [DockerTag.parse(x) for x in img.tags]
    published = [t.version for t in tags if t.registry == registry_hub]
    existing = [t.version for t in tags if t.registry == registry_local]
    for tag in set(existing) - set(published):
        tag_and_push(img, registry_hub, name, tag)
    # Make this the 'release' version of the image
    tag_and_push(img, registry_hub, name, current_release_tag)


# NOTE: Using subprocess here and not the python docker module because
# the latter does not support streaming as nicely as the CLI
def tag_and_push(img, registry_local, name, tag):
    t = DockerTag(registry_local, name, tag)
    img.tag(t.repository, t.version)
    subprocess.run(["docker", "push", str(t)], check=True)


if __name__ == "__main__":
    args = docopt.docopt(__doc__)
    version = args["<version>"]
    if version == "latest":
        version = get_latest_release_tag()
    else:
        validate_release_tag(version)

    if args["tag"]:
        set_image_tags(version)
        if args["--publish"]:
            publish_images(version)
    elif args["publish"]:
        publish_images(version)
