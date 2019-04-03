package org.vaccineimpact.orderlyweb.userCLI

open class Question(val fieldName: String, val default: String? = null)
{
    protected open fun getLine(): String? = readLine()

    fun ask(): String
    {
        var answer: String? = null
        while (answer == null || answer.isBlank())
        {
            answer = getAnswerOrDefault()
        }
        return answer
    }

    private fun getAnswerOrDefault(): String?
    {
        val answer = getAnswer()
        if (answer == null || answer.isBlank())
        {
            if (default != null)
            {
                return default
            }
            else
            {
                println("'$fieldName' cannot be blank")
            }
        }
        return answer
    }

    private fun getAnswer(): String?
    {
        print(fieldName)
        if (default != null)
        {
            print(" [$default]")
        }
        print(": ")
        return getLine()
    }
}
