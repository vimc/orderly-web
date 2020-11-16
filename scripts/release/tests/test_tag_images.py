import sys
from os.path import abspath
sys.path.append(abspath('./scripts/release/'))

from ..tag_images import set_image_tags, publish_images
from unittest import mock
import subprocess
import pytest

containers = ["orderly-web",
        "orderlyweb-migrate",
        "orderly-web-user-cli",
        "orderly-web-css-generator"]

@mock.patch('subprocess.run')
@mock.patch('docker.client.from_env')
def test_set_image_tags(mock_docker, mock_run):

    mock_run.return_value = subprocess.CompletedProcess([], 0, stdout="sha")

    mock_docker_client = mock.MagicMock()
    mock_docker.return_value = mock_docker_client

    mock_img = mock.MagicMock()
    mock_docker_client.images.pull = mock.MagicMock(return_value=mock_img)

    set_image_tags("v1.0.0")

    # assert sha fetched for version
    mock_run.assert_any_call(["git", "rev-list", "-n", "1", "v1.0.0"],
               stdout=subprocess.PIPE, check=True, universal_newlines=True)

    for container in containers:
        # assert that images were pulled for each container
        mock_docker_client.images.pull.assert_any_call("vimc/{}:sha".format(container))

        # assert image was tagged for each container
        mock_img.tag.assert_any_call("vimc/{}".format(container), "v1.0.0")

        # assert that run was called to push image for each container
        mock_run.assert_any_call(["docker", "push", "vimc/{}:v1.0.0".format(container)],
                                 check=True)


@mock.patch('subprocess.run')
@mock.patch('docker.client.from_env')
def test_publish_images(mock_docker, mock_run):
    mock_docker_client = mock.MagicMock()
    mock_docker.return_value = mock_docker_client

    mock_img = mock.MagicMock()
    mock_docker_client.images.get = mock.MagicMock(return_value=mock_img)

    mock_img.tags = [
        "vimc/image:sha",
        "vimc/image:v1.0.0"
    ]

    publish_images("v1.0.0")

    for container in containers:
        # assert that release tag has been pushed to publish registry
        mock_img.tag.assert_any_call("vimc/{}".format(container), "release")
        mock_run.assert_any_call(["docker", "push", "vimc/{}:release".format(container)],
                                 check=True)
