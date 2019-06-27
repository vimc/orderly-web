# OrderlyWeb API

Follows the general points in the [montagu api](https://github.com/vimc/montagu-api/blob/master/docs/spec/spec.md)

* all data is returned in JSON format following the standard response schema defined above
* `POST` data must be sent in JSON format
* All API endpoints can be found relative to `https://[base-url]/api/[version]`, where `[base-url]` is the home url for the 
OrderlyWeb web portal, and `[version]` is the required version of the api.
* The only available version currently is `v1` so all api endpoints can be found relative to
`https://[base-url]/api/v1/` e.g. `https://[base-url]/api/v1/reports/`
* API endpoints can be accessed with or without trailing slashes e.g. both `https://[base-url]/api/v1/reports/` and 
`https://[base-url]/api/v1/reports` will work. 

In addition

* Query parameters that accept booleans are case insensitive and accept `true` and `false`.
* Authentication is via tokens issued by the Montagu API or by GitHub (see [below](#post-login) for details).
* Here are the JSON schema of some response types not covered by the standard data responses described below:
  
  * [`Error.schema.json`](Error.schema.json) - Schema of error responses
  * [`ErrorCode.schema.json`](ErrorCode.schema.json) - Schema of error codes (provided as part of error responses)
  * [`Index.schema.json`](Index.schema.json) - Schema of response found at root url `/` which lists all available endpoints
  * [`Response.schema.json`](Response.schema.json) - The wrapper schema of all responses. 

* For each endpoint, if the user does not have the `reports.review` permission then only published report versions' data 
will be accessible. If the user does have `reports.review` then both published and unpublished report versions will be accessible. 
* In addition, users may have permission scoped at the report level, and will not be able to access reports for which they
do not have read or review permission. 


## POST /login/
To login and retrieve a bearer token that can be used to authenticate all other requests
users will need either a GitHub token or a Montagu token, depending on which provider the app is configured to run with.
 
To authenticate with GitHub, first create a limited scope GitHub token by going [here](https://github.com/settings/tokens)
and choosing `read:user` and `user:email` under section `user` as the only selected scopes.

To authenticate with Montagu, first retrieve a Montatgu token by following the instructions 
[here](https://github.com/vimc/montagu-api/blob/master/docs/spec/Authentication.md#post-authenticate)

Once you have a token from the appropriate provider, make a POST request to `/login/` sending request header 
`Authorization: token MONTAGU_OR_GITHUB_TOKEN`

Like so:

    POST /login/ HTTP/1.1
    Host: server.example.com
    Authorization: token czZCaGRSa3F0MzpnWDFmQmF0M2JW
    Content-Type: application/x-www-form-urlencoded

### Response
If you provide a valid Montagu or GitHub token (for GitHub you must also be a member of the configured org and
 optionally, team) then an OrderlyWeb access_token is returned that can be used in future
requests. Include the access token using the Authorization
header with the format `Authorization: Bearer ACCESS_TOKEN` in future requests to
other  endpoints.

Schema: [`LoginSuccessful.schema.json`](../schemas/LoginSuccessful.schema.json)

#### Example

    {
        "access_token": "2YotnFZFEjr1zCsicMWpAA",
        "token_type": "bearer",
        "expires_in": 3600
    }

Otherwise an error response is returned with status code 401 if the token was invalid, or 403 if the authentication 
provider is GitHub and the user is not a member of the configured GitHub organization or team.

## GET /reports/

Return a list of all reports with minimal metadata - the id, human readable name and latest version of each.

Required permissions: `reports.read`.

Schema: [`Reports.schema.json`](Reports.schema.json)

### Example

```json

  [
    {"name": "minimal", "display_name": "Minimal example", "latest_version": "20161010-121958-d5f0ea63"},
    {"name": "use_resource", "display_name": "Use resources example", "latest_version": "20171011-121958-effh734"}       
  ]

```

## GET /reports/:name/

Returns a list of version names for the named report.

Required permissions: `reports.read`.

Schema: [`Versions.schema.json`](Versions.schema.json)

### Example

```json
[
    "20161006-142357-e80edf58",
    "20161008-123121-59891d61",
    "20161012-220715-756d55c8"
  ]
```

## GET /reports/:name/versions/:version/

Returns metadata about a single report version.

Required permissions: `reports.read`.

Schema: [`Version.schema.json`](Version.schema.json)

### Example

```json
{
    "id": "20161006-142357-e80edf58",
    "name": "minimal",
    "displayname": null,
    "description": null,
    "artefacts": [
      {        
          "format": "staticgraph",
          "description": "A graph of things",
          "files": [
            "mygraph.png"
          ]        
      }
    ],
    "resources": ["source/inputdata.csv"],
    "date": "2016-10-06 14:23:57.0",   
    "data_hashes": {
      "dat": "386f507375907a60176b717016f0a648"
    },
    "published": false,
    "requester": "Funder McFunderface",
    "author": "Researcher McResearcherface"
  }
```

## POST /reports/:name/run/

Starts a new Orderly run of a report named `:name`.

Required permissions: `reports.run`.

Accepts as `POST` body json that will be passed directly through to the report.  This is required when the report requires parameters and is not allowed for reports that do not allow parameters.

Accepts the query parameter `ref`, to try running the report against a particular git reference (e.g., a branch or a commit).  This is not yet actually supported.

Returns information to query the status of the report via the next endpoint

Schema: [`Run.schema.json`](Run.schema.json)

### Example

``` json
{
    "name": "report-name",
    "key": "adjective_animal",
    "path": "/v1/reports/adjective_animal/status"
}
```

## GET /reports/:key/status/

Get the status of a report.

Required permissions: `reports.run`.

This works only for reports that were queued by the runner itself.

Schema: [`Status.schema.json`](Status.schema.json)

### Example

```json
{
    "key": "adjective_animal",
    "status": "success",
    "version": "20170912-091103-41c62920",
    "output": {
        "stderr": [
            "[ name      ]  example",
            "[ id        ]  20170912-091103-41c62920",
            "[ id_file   ]  /var/folders/3z/86tv450j7kb4w5y4wpxj6d5r0000gn/T//RtmpozkWqn/fileaf521bb78e78",
            "[ data      ]  dat: 20 x 2",
            "[ start     ]  2017-09-12 09:11:03",
            "[ end       ]  2017-09-12 09:11:03",
            "[ artefact  ]  mygraph.png: b7de1d29f37d7913392832db6bc49c99",
            "[ commit    ]  example/20170912-091103-41c62920",
            "[ copy      ]",
            "[ success   ]  :)",
            "id:20170912-091103-41c62920"],
        "stdout": [
            "",
            "> png(\"mygraph.png\")",
            "",
            "> par(mar = c(15, 4, 0.5, 0.5))",
            "",
            "> barplot(setNames(dat$number, dat$name), las = 2)",
            "",
            "> dev.off()",
            "null device ",
            "          1 "
        ]
    }
}
```

## POST /reports/:name/versions/:version/publish/

Publish a report.  Sets the status of the "published" flag.  With no parameters sets the flag to `true` but reports can be unpublished by passing the query parameter `?value=false`.

Required permissions: `reports.review`.

Schema: [`Publish.schema.json`](Publish.schema.json)

### Example

``` json
true
```

## GET /reports/git/status/

Get git status.  This does not quite map onto `git status` but includes output from `git status --porcelain=v1` along with branch and hash information.  When running on a server, ideally the `output` section will be an empty array (otherwise branch changing is disabled)

Required permissions: `reports.run`.

## Example

```json
{
  "branch": "master",
  "hash": "1ed7a67351b03cddbb27d5cb8db184fbd8b2ab0c",
  "clean": true,
  "output": []
}
```

## POST /reports/git/fetch/

Fetch from remote git.  This is required before accessing an updated reference (e.g. a remote branch) or a hash not present in the local git tree.  It's always safe because it does not change the working tree

Required permissions: `reports.run`.

## Example

```json
[
  "From /tmp/RtmpT2bd1r/file138f7147a05/demo",
  "   ba72f7a..ed3d168  master     -> origin/master"
]
```


## POST /reports/git/pull/

Pull from remote git.  This updates the working tree.

Required permissions: `reports.run`.

## Example

```json
[
  "Updating ba72f7a..ed3d168",
  "Fast-forward",
  " new | 1 +",
  " 1 file changed, 1 insertion(+)",
  " create mode 100644 new"
]
```
## GET /reports/:name/versions/:version/data/

Gets a dictionary of data names to hashes.

Required permissions: `reports.read`.

Schema: [`Dictionary.schema.json`](Dictionary.schema.json)

## Example

```json
{  
   "dat": "386f507375907a60176b717016f0a648"
}
```

## GET /reports/:name/versions/:version/data/:data/?type=:type

Downloads a data file. Accepts an optional query parameter `type` which can be either `csv` or `rds`.

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/artefacts/

Gets a dictionary of artefact names to hashes.

Required permissions: `reports.read`.

Schema: [`Dictionary.schema.json`](Dictionary.schema.json)

## Example

```json
{
  "mygraph.png": "7360cb2eed3327ff8a677b3598ed7343"
}
```

## GET /reports/:name/versions/:version/artefacts/:artefact/

Downloads an artefact. 

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/resources/

Gets a dictionary of resource names to hashes.

Required permissions: `reports.read`.

Schema: [`Dictionary.schema.json`](Dictionary.schema.json)

## Example

```json
{
  "meta/data.csv": "0bec5bf6f93c547bc9c6774acaf85e1a"
}
```

## GET /reports/:name/versions/:version/resources/:resource/

Downloads a resource. 

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/all/

Downloads a zip file of all files associated with a report version (including data).

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/changelog/

Returns the changelog for a report version, the report creator's record of changes made during the development
of this version.

Required permissions: `reports.review`.

Schema: [`Changelog.schema.json`]( Changelog.schema.json)

### Example

```json
[
  {
    "label": "public",
    "value": "Added graphs",
    "from_file": true,
    "report_version": "20171220-234033-f97cc4f3"
  },
  {
    "label": "internal",
    "value": "Fixed typos in text",
    "from_file": true,
    "report_version": "20171202-074745-4f66ded4"
  }
]
```


## GET /data/csv/:id/

Downloads a data set in csv format.

Required permissions: `reports.read`.

## GET /data/rds/:id/

Download a data set in rds format. 

Required permissions: `reports.read`.

## GET /reports/:name/latest/changelog/

Returns the changelog for latest version of the named report. 

Required permissions: `reports.review`.

Schema: [`Changelog.schema.json`]( Changelog.schema.json)

### Example

```json
[
  {
    "label": "public",
    "value": "Added graphs",
    "from_file": true,
    "report_version": "20171220-234033-f97cc4f3"
  },
  {
    "label": "internal",
    "value": "Fixed typos in text",
    "from_file": true,
    "report_version": "20171202-074745-4f66ded4"
  }
]
```