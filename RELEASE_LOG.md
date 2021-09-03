# v0.0.2

## Tickets
* mrc-290: Versioning script for Orderly Web
* mrc-330: Update spec
* mrc-332: Create Montagu direct auth client
* mrc-338: Specify favicon location
* mrc-340: Filtering on boolean columns returns parent rows without matching children
* mrc-341: Index route is too secure

## Other branches merged in this release
* move_perms_to_orderly



# v0.0.3

## Tickets
* mrc-290: Versioning script for Orderly Web

## Other branches merged in this release

# v1.0.0

## Tickets
* VIMC-2993: Add monitor endpoint for OW
* mrc-290: Versioning script for Orderly Web
* mrc-349: Bug: order of versions in dropdown switcher is wrong
* mrc-352: Backend for showing global reader roles in sidebar
* mrc-353: Get roles that have specific report reading permission for the sidebar
* mrc-355: Remove specific report reading permission from roles in sidebar
* mrc-356: Turn report readers list into only those readers who have individual scoped permissions
* mrc-357: Validate user email when adding a new user in the sidebar
* mrc-358: Frontend for showing global readers in sidebar
* mrc-360: Front-end for showing specific report readers in sidebar
* mrc-360_addPermissionComponent: Front-end for showing specific report readers in sidebar
* mrc-360_refactor: Front-end for showing specific report readers in sidebar
* mrc-360_typeahead: Front-end for showing specific report readers in sidebar

## Other branches merged in this release
* identity_groups

# v1.0.1

## Tickets
* mrc-359: Show global report reading users in sidebar
* mrc-394: Don't show permission management if fine grained perms are turned off
* mrc-395: Should be able to use anchor tags to jump between report and download tabs

## Other branches merged in this release
* fix_selenium
* release_process

# v1.0.2

## Tickets
* mrc-401: Deep linking works but now the page jumps around when you tab

## Other branches merged in this release

# v1.1.0

## Tickets
* mrc-1286: Endpoint for adding new groups
* mrc-1287: Endpoint for adding members to group
* mrc-1288: Endpoint for removing users from group
* mrc-1290: New admin page route
* mrc-1291: Manage roles widget
* mrc-1291_adduser: Manage roles widget
* mrc-1292: Add new role widget
* mrc-1293_new_remove: Manage role permissions widget
* mrc-1293_perms_in_roles: Manage role permissions widget
* mrc-1294: Manage individual user permissions widget
* mrc-1294_controller: Manage individual user permissions widget
* mrc-1303: Endpoint for getting all roles
* mrc-1322: Search widget
* mrc-1326: Add permission to user/role
* mrc-1326_REST: Add permission to user/role
* mrc-1326_movecontroller: Add permission to user/role
* mrc-1326_rename: Add permission to user/role
* mrc-1327: Wire up link to admin page from report page
* mrc-1330: Expand user permission VM to include permissions that come via group memberships
* mrc-1331: User should not be able to remove themselves from admin role
* mrc-1335: Bug: Can't use keys to select values from typeahead
* vimc-3339: Endpoint in OrderlyWeb to add new user

## Other branches merged in this release
* handlebars

# v1.1.1

## Tickets

## Other branches merged in this release

# v1.1.2

## Tickets

## Other branches merged in this release

# v1.2.2

## Tickets
* mrc-1332: Show role permissions as well as direct permissions
* mrc-1333: Front-end integration tests for admin page components
* mrc-1337: Add admin role to migrations
* mrc-1338: Show expander icons for empty list controls which contain an 'Add..' component
* mrc-1339: Bug: Manage role perms: cannot expand role if it has no permissions
* mrc-1342: Create database schema
* mrc-1343: Endpoint to update database tables
* mrc-1344: Documentation index page
* mrc-1344_altschema: Documentation index page
* mrc-1345: Endpoint to serve a static documentation file
* mrc-1345_altschema: Endpoint to serve a static documentation file
* mrc-1346: Add new permission documents.read
* mrc-1349: Endpoint to remove a role
* mrc-1350: Make roles removable in UI
* mrc-1351: Change "Admin" to "Manage access"
* mrc-1352: Refresh roles/users when permissions change
* mrc-1353: Regenerate db interface with new schema
* mrc-1356: Bug: PDF artefact not rendered for DALYs Guidance report in Downloads tab

## Other branches merged in this release
* migrate-local-pwd
* simplify_suggestions

# v1.2.3

## Tickets

## Other branches merged in this release

# v1.2.4

## Tickets

## Other branches merged in this release

# v1.2.5

## Tickets
* mrc-1358: Add all custom fields to the report index table
* mrc-1367: OrderlyWeb release script should handle 'single' and "double" quotes in ticket names
* mrc-1368: Move all logic for publishing reports into the reporting API
* mrc-1370: Render parameters in table view
* mrc-1380: make static documentation tree view collapsible
* mrc-1381: Only show "open" link for documents that can be opened in the browser
* mrc-1382_backend: Come up with way to serve external links as static documentation
* mrc-1382_frontend: Come up with way to serve external links as static documentation
* mrc-1382_schema: Come up with way to serve external links as static documentation
* mrc-1383: Add link to project docs from landing page
* mrc-1388: Tags: OrderlyWeb DB schema
* mrc-1389: Tags: Add permission
* mrc-424: Expired token leads to error
* vimc-3381: Add file size to the download page on the Reporting Portal

## Other branches merged in this release

# v1.2.6

## Tickets
* mrc-1385: Endpoint for getting report metadata
* mrc-1386: Disambiguate the git endpoints
* mrc-1390: Tags: Add getting OW report and version tags to backend for index page
* mrc-1391: Tags: add getting Orderly tags for report versions to backend for index page
* mrc-1392: Tags: display all tags in Index page
* mrc-1394: Tags: add getting OW report and version tags to backend for report page
* mrc-1397: Tags: edit tags backend
* mrc-1403: Support unauthenticated users
* mrc-1404: Style web links differently from files
* mrc-1405: Bug - parameters overflow into next column in reports table
* mrc-1408: Add security testing util for api endpoints
* mrc-1410: documents - style web links as links and use file name as display name

## Other branches merged in this release
* apisecurity

# v1.2.7

## Tickets
* mrc-1393: Tags: Add multi-select filter for tags in index page
* mrc-1396: Tags: Display tags in report page
* mrc-1397_update: Tags: edit tags backend
* mrc-1398: Tags: edit tags frontend
* mrc-1414: Factor out artefact repo
* mrc-1454: Rename "anon" to "guest"
* mrc-1455: Make guest login allowed configurable by admin: backend
* mrc-1456: Prevent the guest user from being assigned certain permissions
* mrc-1458: Update refresh documents endpoint to also download new files first
* mrc-1459: Add documents.manage permission to db
* mrc-1460: Add widget for refreshing documents
* mrc-1460_getdocs: Add widget for refreshing documents
* mrc-1462: Make guest login allowed configurable by admin: frontend
* mrc-1464: Url validation when refreshing documents
* mrc-1476_changelog: Factor out report repo
* mrc-1476_customfields: Factor out report repo
* mrc-1476_getVersions: Factor out report repo
* mrc-1476_models: Factor out report repo
* mrc-1476_params: Factor out report repo
* mrc-1476_repo: Factor out report repo
* mrc-1476_tags2: Factor out report repo
* mrc-1478: Bug - external changelogs missing styles
* mrc-1481: Should not be able to remove permissions from the Admin group itself
* mrc-1490: Make Admin role non-editable in front end
* mrc-1499: Pinned reports should return report's latest published version
* mrc-1500: Endpoint to set global pinned reports
* mrc-1501: Provide report display names in index view model for setting pinned reports
* mrc-1502: Admin front end to set global pinned reports
* mrc-1503: Fix tests for elapsed column in report version table
* mrc-1573: Allow "published" status to survive orderly rebuild
* mrc-1598: Fetch run report metadata and git branches from Orderly on run report page load
* mrc-1600: Run report page: show git branches available
* mrc-1604: Run report page: page and tabs

## Other branches merged in this release
* chromedriver
* dependabot/npm_and_yarn/src/app/static/jquery-3.5.0

# v1.2.8

## Tickets
* mrc-1597: Get available Git commits
* mrc-1615: Integrate orderly.server changes into OrderlyWeb
* vimc-4062: Manually insert rows into orderlyweb_report_version

## Other branches merged in this release

# v1.3.0

## Tickets
* mrc-1484_component: Widget that displays all unpublished report versions
* mrc-1484_page: Widget that displays all unpublished report versions
* mrc-1485_repo: Endpoint to return unpublished report versions, grouped by report and then by date
* mrc-1485_tinyrefactor: Endpoint to return unpublished report versions, grouped by report and then by date
* mrc-1485_vms: Endpoint to return unpublished report versions, grouped by report and then by date
* mrc-1684: Running reports through the web UI reports their status incorrectly
* mrc-1687: Bulk publishing endpoint
* mrc-1688_backend: Publish selected reports from the new publish-reports page
* mrc-1688_refactor_select: Publish selected reports from the new publish-reports page
* mrc-1689: Filter report drafts by those with previously published versions
* mrc-1690: Option to toggle all changelogs at once on bulk publishing page

## Other branches merged in this release
* chromedriver-83
* dependabot/npm_and_yarn/src/app/static/elliptic-6.5.3
* dependabot/npm_and_yarn/src/app/static/lodash-4.17.19
* improve_scripts

# v1.4.0

## Tickets
* mrc-1597_backend: Get available Git commits
* mrc-1601: Use real Orderly server endpoints to get run metadata
* mrc-1601_refactor: Use real Orderly server endpoints to get run metadata
* mrc-1602: Run report page: show instances available
* mrc-1685: Move orderly-web build to buildkite
* mrc-1686: Move top right navigation into a dropdown
* mrc-1812: Add readonly flag to user in db
* mrc-1817: Bug - regenerating the OW db interface breaks the codebase
* mrc-1828: Remove TeamcityTest class
* mrc-1830: Remove teamcity scripts and switch off teamcity build
* mrc-1898: Add kill running report endpoint to OrderlyWeb
* mrc-1908: Expose bundle interface to orderlyweb
* mrc-1976: Ensure ChromeDriver/Chrome compatibility
* mrc-2009: Add Detekt to the OrderlyWeb CI build for Kotlin linting
* mrc-2013: Remove khttp dependency from OrderlyServerAPI
* mrc-2025: Save kotlin HTML test logs as build artefacts
* mrc-2036: Add Codecov to Buildkite
* mrc-2043: Update Kotlin to latest v1.3.x
* mrc-2065: Correctly handle requests with query parameters in OrderlyServerAPI
* vimc-4228: Remove proxy containers from private registry
* vimc-4407: Fix Selenium tests

## Other branches merged in this release

# v1.5.0

## Tickets
* mrc-2116: Add CLI to buildkite build

## Other branches merged in this release

# v1.6.0

## Tickets
* mrc-1599: Endpoint to get all reports user can run
* mrc-1603: Run report page: user can select report to run
* mrc-1605: Run report page: user can populate parameters
* mrc-1605-docker: Run report page: user can populate parameters
* mrc-1606: Run report page: Run report
* mrc-1607: Run report page: user can set changelog message and type
* mrc-1608: Run reports: link from home page
* mrc-2024: Remove final usages of khttp
* mrc-2057: Metrics endpoint has incorrect content type
* mrc-2079: Resolve npm package vulnerabilities
* mrc-2145: Add running report table/columns to orderly web db schema
* mrc-2152: Run report page: 'git fetch' button
* mrc-2154: Make readme setup instructions clearer
* mrc-2155: Ensure compatibility with current version of Node (14/LTS)
* mrc-2161: Pass changelog message to orderly server endpoint
* mrc-2163: Replace Unicode 'x' character in report selector with SVG icon
* mrc-2165: Update orderly web to use new interface
* mrc-2167: Add parameters to run report request
* mrc-2187: Improve documentation about linting
* mrc-2189: Support timeout in run report API endpoint, update docs
* mrc-2190: Implement basic live-reload for front-end code

## Other branches merged in this release
* dependabot/npm_and_yarn/src/app/static/axios-0.21.1
* dependabot/npm_and_yarn/src/app/static/ini-1.3.7

# v1.7.0

## Tickets
* mrc-2171: Mockups for adding metadata to report version page
* mrc-2172: Add metadata tab to report version page
* mrc-2174: Add start and elapsed time to report version page
* mrc-2175: Add git commit and branch information to report version page
* mrc-2176: Add upstream dependencies to report version page
* mrc-2182: Rationalise OrderlyServerAPI.post() methods
* mrc-2188: Create a pull request checklist
* mrc-2205: Correct linting setup
* mrc-2243: Create runReportTab to house runReport and reportLog components
* mrc-2247: Add orderly-web support for queue status endpoint
* mrc-2298: Links in project documentation file tree should open in new tab

## Other branches merged in this release

# v1.7.1

## Tickets

## Other branches merged in this release

# v1.8.1

## Tickets
* VIMC-4596: Change Run report button on ReportVersion page to link to run-report
* mrc-2147: Show available running reports on Running report logs page
* mrc-2148: Fetch and display logs and metadata for selected running report
* mrc-2149: Poll for status updates while running report is incomplete
* mrc-2173: Add report db instance to report version page
* mrc-2198: Accessibility: OrderlyWeb - add report, address issues
* mrc-2302: Create database table/logic for workflows
* mrc-2304: Add endpoint to run a workflow
* mrc-2305: Add endpoint to retrieve list of workflows
* mrc-2307: Refactor navigation for running reports and workflows
* mrc-2332: Use verified version of Codecov uploader
* mrc-2339: Add report name and start time to running report details view
* mrc-2343: Fix database instaces in report log details view
* mrc-2354: Ensure interrupted orderly-web CI builds are cleaned-up
* mrc-2360: Update wording on Accessibility page

## Other branches merged in this release
* dependabot/npm_and_yarn/src/app/static/elliptic-6.5.4
* dependabot/npm_and_yarn/src/app/static/ssri-6.0.2
* dependabot/npm_and_yarn/src/app/static/y18n-3.2.2
* mrc-2303x

# v1.9.0

## Tickets
* mrc-1925: Slow loading with 7k reports
* mrc-2150: Add 'View log' link on run report tab
* mrc-2306: Add endpoint to get status of a workflow
* mrc-2309: Create container component for workflow wizard
* mrc-2344: Persist run report tab and selected running report in session
* mrc-2410: Make Buildkite cleanup hook more verbose/robust
* mrc-2419: Fix Selenium test for report-running progress

## Other branches merged in this release

# v1.10.0

## Tickets

## Other branches merged in this release
* mrc-2308
* mrc-2310
* mrc-2310_git
* mrc-2310_reports
* mrc-2311
* mrc-2311_git
* mrc-2312
* mrc-2312_master
* mrc-2323
* mrc-2323_merge
* mrc-2326
* mrc-2423
* mrc-2467
* mrc-2475
* mrc-2486
* mrc-2511_emit
* mrc-2511_types
* mrc-2513
* mrc-2516
* mrc-2557
* mrc-2558
* mrc-2559
* mrc-2560
* mrc-2562
* mrc-2592
* mwoodbri/master
* path-fix
* revert-347-mrc-2312

