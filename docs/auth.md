# Authenticating with Montagu

![Montagu web auth flow](/docs/montaguindirectauth.png?raw=true) 

This is a version of the `pac4j` UI authentication flow: https://www.pac4j.org/docs/authentication-flows.html
For local development run `./dev/run-dependencies.sh` to run a partial local copy of Montagu that will allow you 
to log in with the test user account `test.user@example.com` and password `password`.

# Authenticating with GitHub
You should make sure that the GitHub org configured in the app config (`./config/default.properties`) has approved 3rd party access for the 
 app `TestOrderlyWeb`. This will involve:
 
1. Running the app and logging in with a user account that has access to the configured org
1. Requesting access from the org: https://help.github.com/en/articles/requesting-organization-approval-for-oauth-apps and
1. Granting org level access to the app: https://help.github.com/en/articles/approving-oauth-apps-for-your-organization

If using the test org `vimc-auth-test` you can log in with the test user account `vimc-auth-test-user` and password
`AfakeP@s5w0rd`. There are no security worries about someone accessing this account because it only has access to
an empty organisation, but it would be very annoying if someone did take it over and change the password,
 so once we have vault access in the build agents we can store a new password in the vault
  (see https://vimc.myjetbrains.com/youtrack/issue/VIMC-2507)
