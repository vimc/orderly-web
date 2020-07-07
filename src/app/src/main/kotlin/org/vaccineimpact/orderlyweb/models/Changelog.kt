package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties

data class Changelog
@ConstructorProperties("reportVersion", "label", "value", "public")
constructor(val reportVersion: String,
            val label: String,
            val value: String,
            val public: Boolean)
