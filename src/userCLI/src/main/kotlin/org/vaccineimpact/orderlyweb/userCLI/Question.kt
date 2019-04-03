package org.vaccineimpact.orderlyweb.userCLI

interface Question {
    fun ask(): String
}

class CommandLineQuestion(val fieldName: String, val default: String? = null): Question
{
    private fun getLine(): String? = readLine()

    override fun ask(): String
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
