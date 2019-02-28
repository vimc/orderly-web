package org.vaccineimpact.orderlyweb.db

class UnableToConnectToDatabase(val url: String) : Exception(
        "Unable to connect to database at $url"
)