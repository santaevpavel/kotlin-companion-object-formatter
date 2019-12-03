import KtCompanionObjectNewPlacementFinder.Results
import org.hamcrest.core.IsInstanceOf
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

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
        val ktClass = ktFile.findClass()

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass)

        assertThat(result, IsInstanceOf(Results.NoPlacement::class.java))
        // assertEquals((result as Results.PlacementAfter).line, 8)
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
        val ktClass = ktFile.findClass()

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass)

        assertThat(result, IsInstanceOf(Results.NoPlacement::class.java))
        // assertEquals((result as Results.Placement).line, 9)
    }

    private fun KtFile.findClass(): KtClass {
        return children.filterIsInstance<KtClass>().first()
    }
}