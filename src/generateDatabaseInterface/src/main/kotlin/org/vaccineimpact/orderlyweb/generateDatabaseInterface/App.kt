package org.vaccineimpact.orderlyweb.generateDatabaseInterface

import org.jooq.codegen.GenerationTool

fun main(args: Array<String>)
{
    CodeGenerator().run()
}

class CodeGenerator
{
    fun run()
    {
        val url = CodeGenerator::class.java.classLoader.getResource("config.xml")
        val config = url.openStream().use {
            GenerationTool.load(it)
        }
        GenerationTool().run(config)
    }
}