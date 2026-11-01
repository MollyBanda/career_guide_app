package com.pathfinder.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Basic Android instrumentation smoke check, separate from the submitted PDF cases. */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test fun applicationUsesThePathwisePackageId() {
        assertEquals("com.pathfinder.app", InstrumentationRegistry.getInstrumentation().targetContext.packageName)
    }
}
