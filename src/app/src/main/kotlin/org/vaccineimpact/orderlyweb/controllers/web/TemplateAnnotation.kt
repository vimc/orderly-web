package org.vaccineimpact.orderlyweb.controllers.web

@Target(AnnotationTarget.FUNCTION)
annotation class Template(val templateName: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Serialise(val propertyName: String)
