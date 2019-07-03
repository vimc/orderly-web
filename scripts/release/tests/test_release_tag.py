import sys
from os.path import abspath
sys.path.append(abspath('./scripts/release/'))

from ..release_tag import get_latest_release_tag, validate_release_tag, version_greater_than
from unittest import mock
import subprocess
import pytest


mock_run_get_latest_version_results = [
    "",  # first call to run does a fetch
    "non-version-tag\nv1.0.0\nv0.2.0\nv0.1.1"  # second call gets tags
]


def mock_run_get_latest_version(parts, check, stdout, universal_newlines, cwd):
    return subprocess.CompletedProcess([], 0, stdout=mock_run_get_latest_version_results.pop(0))


@mock.patch('subprocess.run', mock_run_get_latest_version)
def test_get_latest_tag():
    assert get_latest_release_tag() == "v1.0.0"


def test_validate_release_tag_valid():
    assert validate_release_tag("v1.2.3").group(0) == "v1.2.3"


def test_validate_release_tag_invalid():
    with pytest.raises(Exception):
        validate_release_tag("nothing doing")


def test_version_greater_than():
    assert version_greater_than("v1.0.0", "v0.99.98") is True
    assert version_greater_than("v0.99.98", "v1.0.0") is False
    assert version_greater_than("v1.0.0", "v1.0.0") is False
