import sys
from os.path import abspath
sys.path.append(abspath('./scripts/release/'))
from ..branch_diff import Difference, Branch
from unittest import mock
import subprocess


mock_run_difference_init_results = [
    "current",  # first call to run checks current version sha
    "master\nbranch1\nbranch2\nbranch3\nbranch4",  # second call gets merged branches for current version
    "master\nbranch1\nbranch2"  # third call gets merged branches for past version
]


def mock_run_difference_init(parts, check, stdout, universal_newlines, cwd):
    return subprocess.CompletedProcess([], 0, stdout=mock_run_difference_init_results.pop(0))


@mock.patch('subprocess.run', mock_run_difference_init)
def test_initialises_with_branch_diff():
    sut = Difference('past_version')
    assert sut.branches == {"branch3", "branch4"}


def test_parse_branch():
    sut = Branch.parse("origin/branch1")
    assert sut.remote == "origin"
    assert sut.name == "branch1"
