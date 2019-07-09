from helpers import run

class Branch:
    def __init__(self, remote, name):
        self.remote = remote
        self.name = name

    @classmethod
    def parse(cls, raw):
        raw = raw.strip()
        if " -> " in raw:
            return None
        else:
            split_point = raw.find('/')
            if split_point:
                return Branch(raw[:split_point], raw[(split_point + 1):])
            else:
                raise Exception("Unable to parse branch '{}'".format(raw))


class Difference:
    def __init__(self, past_version):
        """We store the difference since last version as a list of branches"""
        self.diff = Difference.get_branch_diff(past=past_version)

    @property
    def branches(self):
        return self.diff

    @classmethod
    def get_branches_at(cls, revision):
        raw = run("git branch -r --merged {}".format(revision))
        try:
            parsed = [Branch.parse(b) for b in raw.split('\n')]
            branches = set(b.name for b in parsed if b)
        except Exception as e:
            ex_template = "For repository {}, this error occurred: {}"
            raise Exception(ex_template.format(working_dir, e))
        return branches

    @classmethod
    def get_branch_diff(cls, current=None, past=None):
        current = current or run("git rev-parse --short=7 HEAD")
        branches_now = cls.get_branches_at(current)
        branches_then = cls.get_branches_at(past)
        return (branches_now - branches_then) - set(["master"])
