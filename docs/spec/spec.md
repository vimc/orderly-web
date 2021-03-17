# OrderlyWeb API

# General points
* By default data returned is in JSON format, and POST and PUT data is expected 
  as a string containing JSON-formatted data. Some endpoints return other data
  as described in the individual endpoint descriptions.
* Dates and times are returned as strings according to the ISO 8601 standard.
* Unless otherwise noted, URLs and IDs embedded in URLs are case-sensitive.
* You must include the correct accept-header for each request: `application/json` for 
  json endpoints and `text/csv` for csv endpoints. If your accept-header is incorrectly 
  set for the endpoint you are accessing, you may get a `Error: Unknown resource. 
  Please check the URL` even though your URL may be correct.
* All API endpoints can be found relative to `https://[base-url]/api/[version]`, where `[base-url]` is the home url for the 
  OrderlyWeb web portal, and `[version]` is the required version of the api.
* The only available version currently is `v1` so all api endpoints can be found relative to
  `https://[base-url]/api/v1/` e.g. `https://[base-url]/api/v1/reports/`
* API endpoints can be accessed with or without trailing slashes e.g. both `https://[base-url]/api/v1/reports/` and 
  `https://[base-url]/api/v1/reports` will work. 
* Query parameters that accept booleans are case insensitive and accept `true` and `false`.
* Authentication is via tokens issued by the Montagu API or by GitHub (see [below](#post-login) for details).
* For each endpoint, if the user does not have the `reports.review` permission then only published report versions' data 
will be accessible. If the user does have `reports.review` then both published and unpublished report versions will be accessible. 
* In addition, users may have permission scoped at the report level, and will not be able to access reports for which they
do not have read or review permission.   

# Standard response format
All responses are returned in a standard format. Throughout this specification, 
wherever an endpoint describes its response format, it should be assumed the payload is wrapped in
the standard response format, so that the `data` property holds the payload.

## Success
Schema: [`Response.schema.json`](Response.schema.json)

### Example
    {
        "status": "success",
        "data": {},
        "errors": []
    }

## Error
Schema: [`Response.schema.json`](Response.schema.json)

### Example
    {
        "status": "failure",
        "data": null,
        "errors": [
            { 
                "code": "unique-error-code", 
                "message": "Full, user-friendly error message" 
            }
        ]
    }

# Standard response codes
The complete list of HTTP status codes returned by the API is:
* 200 - OK
* 201 - Object successfully created
* 400 - Bad request
* 404 - Resource not found. This will be returned both when the url is not part of the spec,
and when a requested resource does not exist 
* 401 - Unauthorized - User is not logged in
* 403 - Forbidden - User does not have required permissions
* 409 - Conflict. This will be returned when the user's request attempts to insert an object into
the database with a primary key that already exists.
* 500 - Internal server error - This generally indicates an unexpected error has occurred. 

*Note that we use 400 liberally to indicate when submitted data
does not conform to expected OrderlyWeb conventions, as well as for invalid operation
 requests.

#Endpoints
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

## GET /

Return a description of all endpoints available over the api. 

Required permissions: none

Schema: [`Index.schema.json`](Index.schema.json)

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

Schema: [`VersionIds.schema.json`](VersionIds.schema.json)

### Example

```json
[
    "20161006-142357-e80edf58",
    "20161008-123121-59891d61",
    "20161012-220715-756d55c8"
  ]
```

## GET /reports/:name/versions/:version/

Returns full metadata about a single report version.

Required permissions: `reports.read`.

Schema: [`VersionDetails.schema.json`](VersionDetails.schema.json)

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

Accepts optional arguments as the JSON encoded body of a `POST` request:
```json
{
  "instances": {"source": "production"},
  "params": {"name1": "value1", "name2": "value2"},
  "gitBranch": "main",
  "gitCommit": "abc1234"
}
```
`params` will be passed directly through to the report.  This is required when the report requires parameters and is not allowed for reports that do not allow parameters.

`gitBranch` and `gitCommit` indicate which git version of the report should be run

`instances` indicate which database instance should be used. Currently using only one instance is supported.

Accepts the query parameter `timeout`, which sets the the number of seconds to wait before the job is terminated.  The default is 3 hours (10800 seconds).

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

Accepts query parameter `output`, which if TRUE returns the log from the job run.

This works only for reports that were queued by the runner itself.

Schema: [`Status.schema.json`](Status.schema.json)

### Example queued status

```json
{
    "key": "adjective_animal",
    "status": "queued",
    "version": null,
    "output": null,
    "queue": [
        {
            "key": "antiutopian_peregrinefalcon",
            "status": "running",
            "name": "minimal"
        },
        {
            "key": "flavoured_bassethound",
            "status": "queued",
            "name": "other"
        }
    ]
}
```

### Example completed status

```json
{
    "key": "adjective_animal",
    "status": "success",
    "version": "20170912-091103-41c62920",
    "output": [
        "[ name      ]  example",
        "[ id        ]  20170912-091103-41c62920",
        "[ id_file   ]  /var/folders/3z/86tv450j7kb4w5y4wpxj6d5r0000gn/T//RtmpozkWqn/fileaf521bb78e78",
        "[ data      ]  dat: 20 x 2",
        "[ start     ]  2017-09-12 09:11:03",
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
        "[ end       ]  2017-09-12 09:11:03",
        "[ artefact  ]  mygraph.png: b7de1d29f37d7913392832db6bc49c99",
        "[ commit    ]  example/20170912-091103-41c62920",
        "[ copy      ]",
        "[ success   ]  :)",
        "id:20170912-091103-41c62920"
    ],
    "queue": []
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

## GET /git/status/

Get git status.  This does not quite map onto `git status` but includes output from `git status --porcelain=v1` along with branch and hash information.  When running on a server, ideally the `output` section will be an empty array (otherwise branch changing is disabled)

Required permissions: `reports.run`.

Schema: [`GitStatus.schema.json`](GitStatus.schema.json)

## Example

```json
{
  "branch": "master",
  "hash": "1ed7a67351b03cddbb27d5cb8db184fbd8b2ab0c",
  "clean": true,
  "output": []
}
```

## POST /git/fetch/

Fetch from remote git.  This is required before accessing an updated reference (e.g. a remote branch) or a hash not present in the local git tree.  It's always safe because it does not change the working tree

Required permissions: `reports.run`.

Schema: [`GitFetch.schema.json`](GitFetch.schema.json)

## Example

```json
[
  "From /tmp/RtmpT2bd1r/file138f7147a05/demo",
  "   ba72f7a..ed3d168  master     -> origin/master"
]
```

## POST /git/pull/

Pull from remote git.  This updates the working tree.

Required permissions: `reports.run`.

Schema: [`GitPull.schema.json`](GitPull.schema.json)

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

If the requesting user has `reports.review` permission, downloads a zip file of all files associated with a report 
version (including data). If the user only has `reports.read` permission, a zip file containing the report version's
artefacts, resources and readme file only will be downloaded.  

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/changelog/

Returns the changelog for a report version, the report creator's record of changes made during the development
of this version. 

`Reports.read` is the minimum permission required. Users who have `reports.read` permission only will be able to see
public changelog entries. Users with `reports.review` permission will also see internal changelog entries. 

Required permissions: `reports.read`.

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

## GET /reports/:name/latest/changelog/

Returns the changelog for latest version of the named report. 

`Reports.read` is the minimum permission required. Users who have `reports.read` permission only will be able to see
public changelog entries. Users with `reports.review` permission will also see internal changelog entries. 

The changelog returned will belong to the the latest report version which is accessible to the user. For readers with 
`report.read` permission only, this will be the latest public version. For readers with `report.review` permission it
will be the latest published or unpublished version. 

Required permissions: `reports.read`.

Schema: [`Changelog.schema.json`](Changelog.schema.json)

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

## GET /versions/

Gets metadata of all report versions accessible to the user. 

Required permissions: `reports.read`

Schema: [`Versions.schema.json`](Versions.schema.json)

### Example

```json
[
  {
    "id": "20161006-142357-e80edf58",
    "name": "minimal",
    "displayname": null,  
    "resources": ["source/inputdata.csv"],
    "date": "2016-10-06 14:23:57.0",   
    "published": false,
    "requester": "Funder McFunderface",
    "author": "Researcher McResearcherface"
  },
  {
      "id": "20161106-152357-e80edf92",
      "name": "another name",
      "displayname": null,  
      "resources": ["source/inputdata.csv"],
      "date": "2016-11-06 15:23:57.0",   
      "published": true,
      "requester": "Funder McFunderface",
      "author": "Researcher McResearcherface"
    }
]  
```

## GET /data/csv/:id/

Downloads a data set in csv format.

Required permissions: `reports.read`.

## GET /data/rds/:id/

Download a data set in rds format. 

Required permissions: `reports.read`.

## POST /bundle/pack/:name/

Downloads a [bundle](https://www.vaccineimpact.org/orderly/articles/bundles.html) for the named report in zip format. The bundle is expected to be "completed" (i.e. run locally) before being imported.

Required permissions: `reports.run`.

## POST /bundle/import/

Imports a completed bundle in zip format.

Required permissions: `reports.run`.

## GET /queue/status/

Gets information on the current state of the orderly queue.

Required permissions: none, available to all authenticated users

Schema: [`QueueStatus.schema.json`](QueueStatus.schema.json)

### Example
```json
{
  "tasks": [
    {
        "key": "antiutopian_peregrinefalcon",
        "status": "running",
        "name": "minimal"
    },
    {
        "key": "flavoured_bassethound",
        "status": "queued",
        "name": "other"
    }
  ]
}
```
