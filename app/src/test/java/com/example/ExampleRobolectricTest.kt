package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MathDifficulty
import com.example.util.MathPuzzleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WakeHero", appName)
  }

  @Test
  fun `math puzzle generator produces valid problems`() {
    for (difficulty in MathDifficulty.values()) {
      val problem = MathPuzzleGenerator.generateProblem(difficulty)
      assertNotNull(problem.expression)
      assertNotNull(problem.solution)
    }
  }
}
