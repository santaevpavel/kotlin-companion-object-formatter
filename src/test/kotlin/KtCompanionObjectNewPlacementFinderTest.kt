import KtCompanionObjectNewPlacementFinder.Result
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import utils.KtFunctionFinder
import utils.KtСlassFinder

class KtCompanionObjectNewPlacementFinderTest {

    private lateinit var placementFinder: KtCompanionObjectNewPlacementFinder

    @Before
    fun setUp() {
        placementFinder = KtCompanionObjectNewPlacementFinder()
    }

    @Test
    fun `should find bottom of class`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val ktClass = KtСlassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Result.PlacementAfter::class.java))
        assertEquals((result as KtCompanionObjectNewPlacementFinder.Result.PlacementAfter).element, barFunction)
    }

    @Test
    fun `should find bottom of class above inner class`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                class InnerClass {
                    fun foo() : Int {
                        return 1
                    }
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val ktClass = KtСlassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Result.PlacementAfter::class.java))
        assertEquals((result as KtCompanionObjectNewPlacementFinder.Result.PlacementAfter).element, barFunction)
    }

    @Test
    fun `should find bottom of class with inner class`() {
        val file =
            """class Sample {

                companion object {
                    private const val A = "a"
                }

                fun foo(): Int = 0

                private fun func(): Int {
                    return 0
                }

                class InnerClass {
                    fun foo() : Int {
                        return 2
                    }
                }

                private fun bar() = 1
            }
            """
        val ktFile = KtFileReader().fromString(file)

        val ktClass = KtСlassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Result.PlacementAfter::class.java))
        assertEquals((result as KtCompanionObjectNewPlacementFinder.Result.PlacementAfter).element, barFunction)
    }

    @Test
    fun `should find bottom of inner class`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                inner class Inner {
                    fun fooInner(): Int = 0

                    companion object {
                        private const val A = "a"
                    }
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val ktClass = KtСlassFinder("Inner").also { ktFile.accept(it) }.klass
        val fooInnerFunction = KtFunctionFinder("fooInner").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Result.PlacementAfter::class.java))
        assertEquals((result as KtCompanionObjectNewPlacementFinder.Result.PlacementAfter).element, fooInnerFunction)
    }

}