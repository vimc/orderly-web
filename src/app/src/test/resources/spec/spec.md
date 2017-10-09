# Montagu Reporting API

Follows the general points in the [montagu api](https://github.com/vimc/montagu-api/blob/master/spec/spec.md)

* all data is returned in JSON format
* `POST` data must be sent in JSON format
* The canonical form for all URLs (not including query string) ends in a slash: `/`
* The API will be versioned via URL. So for version 1, all URLs will begin /v1/. e.g. http://.../v1/reports/

In addition

* Query parameters that accept booleans are case insensitive and accept `true` and `false`.

Some files are directly copied over (with only whitespace changes) from `montagu-api`:

* `Error.schema.json`
* `ErrorCode.schema.json`
* `Index.schema.json`
* `Response.schema.json`

## GET /reports/

Return a list of all report names

Schema: [`Report.schema.json`](Reports.schema.json)

## GET /reports/:name/

Returns a list of version names for the named report

Schema: [`Report.schema.json`](Report.schema.json)

## GET /reports/:name/:version/

Returns metadata about a single report version

Schema: [`Report.schema.json`](Version.schema.json)

## POST /reports/:name/run/

Try and run a report `:name`

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

## POST /reports/:name/:version/publish/

Publish a report.  Sets the status of the "published" flag.  With no parameters sets the flag to `true` but reports can be unpublished by passing the query parameter `?value=false`.

Schema: [`Publish.schema.json`](Publish.schema.json)

### Example

``` json
true
```

## GET /reports/git/status/

Get git status.  This does not quite map onto `git status` but includes output from `git status --porcelain=v1` along with branch and hash informationl.  When running on a server, ideally the `output` section will be an empty array (otherwise branch changing is disabled)

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

## Example

```json
[
  "From /tmp/RtmpT2bd1r/file138f7147a05/demo",
  "   ba72f7a..ed3d168  master     -> origin/master"
]
```


## POST /reports/git/pull/

Pull from remote git.  This updates the working tree

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
## GET /reports/:name/:version/data/

Gets a dict of data names to hashes

```json
{
  
}
```

## GET /reports/:name/:version/data/:data/?type=:type

Downloads a data file. Accepts an optional query parameter `type` which can be either `csv` or `rds`.


## GET /reports/:name/:version/artefacts/

Gets a dict of artefact names to hashes

```json
{
  
}
```

## GET /reports/:name/:version/artefacts/:artefact/

Downloads an artefact. 


## GET /reports/:name/:version/resources/

Gets a dict of resource names to hashes

```json
{
  
}
```

## GET /reports/:name/:version/resources/:resource/

Downloads a resource. 

## GET /reports/:name/:version/all/

Downloads a zip file of everything (including data)

## GET /data/csv/:id/

Downloads a data set in csv format.

## GET /data/rds/:id/

Download a data set in rds format. 