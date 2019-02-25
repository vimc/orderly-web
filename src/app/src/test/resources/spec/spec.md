# Montagu Reporting API

Follows the general points in the [montagu api](https://github.com/vimc/montagu-api/blob/master/spec/spec.md)

* all data is returned in JSON format following the standard response schema defined above
* `POST` data must be sent in JSON format
* The canonical form for all URLs (not including query string) ends in a slash: `/`
* The API will be versioned via URL. So for version 1, all URLs will begin /v1/. e.g. http://.../v1/reports/

In addition

* Query parameters that accept booleans are case insensitive and accept `true` and `false`.
* Authentication is via tokens issues by the Montagu API

Some files are directly copied over (with only whitespace changes) from `montagu-api`:

* `Error.schema.json`
* `ErrorCode.schema.json`
* `Index.schema.json`
* `Response.schema.json`

For each endpoint, if the user does not have the `reports.review` permission then only published reports will be 
accessible. If the user does have `reports.review` then all reports will be accessible.

## GET /reports/

Return a list of all reports with minimal metadata - the id, human readable name, latest version of each and
whether that version is published.

Required permissions: `reports.read`.

Schema: [`Reports.schema.json`](Reports.schema.json)

### Example

```json

  [
    {"name": "minimal", "display_name": "Minimal example", "latest_version": "20161010-121958-d5f0ea63", "published": "true"},
    {"name": "use_resource", "display_name": "Use resources example", "latest_version": "20171011-121958-effh734", "published": "false"}       
  ]

```

## GET /reports/:name/

Returns a list of version names for the named report.

Required permissions: `reports.read`.

Schema: [`Versions.schema.json`](Version.schema.json)

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

Schema: [`Report.schema.json`](Version.schema.json)

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

Try and run a report `:name`.

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

This works only for reports that were queued by the runner itself/

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

Get git status.  This does not quite map onto `git status` but includes output from `git status --porcelain=v1` along with branch and hash informationl.  When running on a server, ideally the `output` section will be an empty array (otherwise branch changing is disabled)

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

Gets a dict of data names to hashes.

Required permissions: `reports.read`.


```json
{  
   "dat": "386f507375907a60176b717016f0a648"
}
```

## GET /reports/:name/versions/:version/data/:data/?type=:type

Downloads a data file. Accepts an optional query parameter `type` which can be either `csv` or `rds`.

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/artefacts/

Gets a dict of artefact names to hashes.

Required permissions: `reports.read`.

```json
{
  "mygraph.png": "7360cb2eed3327ff8a677b3598ed7343"
}
```

## GET /reports/:name/versions/:version/artefacts/:artefact/

Downloads an artefact. 

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/resources/

Gets a dict of resource names to hashes.

Required permissions: `reports.read`.

```json
{
  "meta/data.csv": "0bec5bf6f93c547bc9c6774acaf85e1a"
}
```

## GET /reports/:name/versions/:version/resources/:resource/

Downloads a resource. 

Required permissions: `reports.read`.

## GET /reports/:name/versions/:version/all/

Downloads a zip file of everything (including data).

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