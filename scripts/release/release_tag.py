import re
from functools import total_ordering
from helpers import run, fetch

release_tag_pattern = re.compile(r"^v(\d+)\.(\d+)\.(\d+)(?:-RC(\d+))?$")


@total_ordering
class ReleaseTag:
    def __init__(self, major, minor, patch, release_candidate):
        self.major = major
        self.minor = minor
        self.patch = patch
        self.release_candidate = release_candidate

    def __gt__(self, other):
        return self.as_list() > other.as_list()

    def __eq__(self, other):
        return self.major == other.major \
            and self.minor == other.minor \
            and self.patch == other.patch \
            and self.release_candidate == other.release_candidate

    def __repr__(self):
        s = "v{}.{}.{}".format(self.major, self.minor, self.patch)
        if self.release_candidate:
            s += "-RC" + self.release_candidate
        return s

    def as_list(self):
        return [self.major, self.minor, self.patch, self.release_candidate or float('inf')]

    @classmethod
    def parse(cls, tag):
        v = cls.validate(tag).groups()
        if v[3]:
            rc = float(v[3])
        else:
            rc = None

        return ReleaseTag(int(v[0]), int(v[1]), int(v[2]), rc)

    @classmethod
    def validate(cls, tag):
        m = release_tag_pattern.match(tag)
        if not m:
            raise Exception("Tag {} does not correspond to pattern".format(tag))

        return m


def get_latest_release_tag():
    fetch()
    tags = run("git tag").split('\n')
    release_tags = sorted(ReleaseTag.parse(t) for t in tags if release_tag_pattern.match(t))

    return str(release_tags[-1])


def version_greater_than(target, than):
    return ReleaseTag.parse(target) > ReleaseTag.parse(than)


def validate_release_tag(tag):
    return ReleaseTag.validate(tag)
