package org.vaccineimpact.orderlyweb.errors

class UnsupportedValueException(val value: Any)
    : Exception("Unsupported value '$value' of type '${value::class.simpleName}'")