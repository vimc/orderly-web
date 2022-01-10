import os
import re
import requests

from branch_diff import Difference

branch_pattern = re.compile(r"^(.+-\d+)($|[_-])")
NOT_FOUND = "NOT FOUND"


def get_token():
    token = os.environ.get('YOUTRACK_TOKEN')
    if not token:
        print("""Please set your YouTrack token as follows:
    export YOUTRACK_TOKEN=xxx
You may wish to add this line to your Bash ~/.profile

You can obtain a new token by following the instructions at
https://www.jetbrains.com/help/youtrack/standalone/Manage-Permanent-Token.html
""")
        exit(-1)
    return token


class Ticket:
    def __init__(self, data):
        self.id = data["idReadable"]
        self.fields = dict(
            (
                customField["projectCustomField"]["field"]["name"],
                customField["value"]["name"] if isinstance(customField["value"], dict) else customField["value"],
            )
            for customField in data["customFields"]
        )
        self.fields["summary"] = data["summary"]

    def get(self, field):
        return self.fields[field]

    def state(self):
        return self.get("State")

    def okay_to_release(self):
        return "Ready to deploy" in self.get("State")


class YouTrackHelper:
    base_url = "https://mrc-ide.myjetbrains.com/youtrack/api/"

    def __init__(self):
        self.token = get_token()

    def get_tickets(self, branch_names):
        branch_names = sorted(branch_names)
        for branch in branch_names:
            match = branch_pattern.match(branch)
            if match:
                yield self.get_ticket(branch, match.group(1))
            else:
                yield branch, NOT_FOUND

    def get_ticket(self, branch, full_id):
        template = "issues/{full_id}?fields=idReadable,summary,customFields(projectCustomField(field(name)),value(name))"
        r = self.request(template.format(full_id=full_id))
        if r.status_code == 200:
            return branch, Ticket(r.json())
        else:
            return branch, NOT_FOUND

    def add_build_tag(self, tag):
        data = {"name": tag, "type": "VersionBundleElement"}
        # 79-25 is "Fixed in OrderlyWeb build" field for mrc project (78-1)
        # See https://www.jetbrains.com/help/youtrack/devportal/api-usecase-add-value-to-bundle.html
        r = self.request("admin/projects/78-1/customFields/79-25/bundle/values?fields=id,name", data)
        # 400 implies already exists
        return r.status_code in [200, 400], r

    def modify_ticket(self, full_id, command):
        data = {"issues": [{"idReadable": full_id}], "query": command}
        r = self.request("commands", data)
        return r.status_code == 200, r

    def request(self, url_fragment, json=None):
        headers = {
            "Authorization": "Bearer " + self.token,
            "Accept": "application/json"
        }
        if json is None:
            method = "get"
        else:
            method = "post"
            headers["Content-Type"] = "application/json"
        url = self.base_url + url_fragment
        response = requests.request(method, url, headers=headers, json=json)
        if response.status_code == 401:
            raise Exception("Failed to authorize against YouTrack")

        return response

def check_ticket(branch, ticket):
    problem = False
    print("* " + branch, end="")
    if ticket == NOT_FOUND:
        print("\n  Warning: Unable to find ticket corresponding to branch " + branch,
              end="")
        problem = True
    else:
        print(": {ticket} ({summary})".format(ticket=ticket.id,
                                              summary=ticket.get("summary")),
              end="")
        if not ticket.okay_to_release():
            print("\n  Warning: Ticket is {state}".format(state=ticket.state()),
                  end="")
            problem = True
    print("")
    return problem


def check_tickets(latest_tag):
    diff = Difference(latest_tag)
    yt = YouTrackHelper()
    pairs = list(yt.get_tickets(diff.branches))
    problems = False

    template = "\nSince {}, the following branches have been merged in:"
    print(template.format(latest_tag))
    for (branch, ticket) in pairs:
        had_problem = check_ticket(branch, ticket)
        problems = problems or had_problem

    if problems:
        answer = input("\nAre you sure you want to proceed? (y/N) ")
        if answer != "y":
            exit(-1)

    return pairs


def tag_tickets(tickets, tag):
    yt = YouTrackHelper()
    problems = []

    success, response = yt.add_build_tag(tag)
    if not success:
        template = "Failed to create new tag {tag}. {status}: {text}"
        problems.append(template.format(tag=tag,
                                        status=response.status_code,
                                        text=response.text))
        return problems

    for ticket in tickets:
        if ticket == NOT_FOUND:
            continue
        success, response = yt.modify_ticket(ticket.id, "Fixed in OrderlyWeb build " + tag)
        if not success:
            template = "Failed to tag {id}. {status}: {text}"
            problems.append(template.format(id=ticket.id,
                                            status=response.status_code,
                                            text=response.text))
    return problems
