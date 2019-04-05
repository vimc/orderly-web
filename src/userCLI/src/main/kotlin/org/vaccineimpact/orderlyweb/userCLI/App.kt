package org.vaccineimpact.orderlyweb.userCLI

import kotlin.system.exitProcess

fun main(args: Array<String>)
{
    val action = getAction(args)
    try
    {
        val remainder = args.drop(1)
        when (action)
        {
            Action.addUser -> addUser(remainder)
            Action.addPermission -> addPermission(remainder)
        }
    }
    catch (e: Exception)
    {
        println(e.message)
        exitProcess(-1)
    }
}

fun getAction(args: Array<String>): Action
{
    if (args.isEmpty())
    {
        println("An action is required. ./user.sh ACTION")
        println("ACTION must be one of " + enumValues<Action>().joinToString())
        exitProcess(0)
    }
    return enumValueOf(args.first())
}

fun addUser(args: List<String>)
{
    println("Coming soon!")
}

fun addPermission(args: List<String>)
{
    println("Coming soon!")
}