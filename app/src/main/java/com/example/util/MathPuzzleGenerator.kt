package com.example.util

import com.example.data.model.MathDifficulty
import kotlin.random.Random

data class MathProblem(
    val expression: String,
    val solution: Int
)

object MathPuzzleGenerator {

    fun generateProblem(difficulty: MathDifficulty): MathProblem {
        return when (difficulty) {
            MathDifficulty.EASY -> generateEasy()
            MathDifficulty.MEDIUM -> generateMedium()
            MathDifficulty.HARD -> generateHard()
            MathDifficulty.GENIUS -> generateGenius()
        }
    }

    private fun generateEasy(): MathProblem {
        val a = Random.nextInt(5, 30)
        val b = Random.nextInt(3, 20)
        val isAdd = Random.nextBoolean()
        return if (isAdd) {
            MathProblem("$a + $b", a + b)
        } else {
            val max = maxOf(a, b)
            val min = minOf(a, b)
            MathProblem("$max - $min", max - min)
        }
    }

    private fun generateMedium(): MathProblem {
        val a = Random.nextInt(15, 60)
        val b = Random.nextInt(10, 45)
        val c = Random.nextInt(5, 30)
        val op1IsAdd = Random.nextBoolean()
        val op2IsAdd = Random.nextBoolean()

        var result = a
        val expr = StringBuilder("$a")

        if (op1IsAdd) {
            result += b
            expr.append(" + $b")
        } else {
            result -= b
            expr.append(" - $b")
        }

        if (op2IsAdd) {
            result += c
            expr.append(" + $c")
        } else {
            result -= c
            expr.append(" - $c")
        }

        return MathProblem(expr.toString(), result)
    }

    private fun generateHard(): MathProblem {
        val a = Random.nextInt(6, 18)
        val b = Random.nextInt(4, 12)
        val c = Random.nextInt(10, 40)
        val product = a * b
        val isAdd = Random.nextBoolean()

        return if (isAdd) {
            MathProblem("$a × $b + $c", product + c)
        } else {
            MathProblem("$a × $b - $c", product - c)
        }
    }

    private fun generateGenius(): MathProblem {
        val a = Random.nextInt(11, 25)
        val b = Random.nextInt(5, 14)
        val divisor = Random.nextInt(2, 6)
        val divResult = Random.nextInt(4, 15)
        val dividend = divisor * divResult

        val product = a * b
        val isAdd = Random.nextBoolean()

        return if (isAdd) {
            MathProblem("($a × $b) + ($dividend ÷ $divisor)", product + divResult)
        } else {
            MathProblem("($a × $b) - ($dividend ÷ $divisor)", product - divResult)
        }
    }
}
