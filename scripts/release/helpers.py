import subprocess
import shlex


def fetch():
    print("Fetching from remote...")
    run("git fetch --tags --all")


def run(cmd, working_dir=None):
    parts = shlex.split(cmd)
    result = subprocess.run(parts, check=True, stdout=subprocess.PIPE,
                            universal_newlines=True, cwd=working_dir)
    return result.stdout.strip()
