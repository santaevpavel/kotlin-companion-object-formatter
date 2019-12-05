import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test
import utils.KtFunctionFinder

class CompanionObjectMoverTest {

    private lateinit var companionObjectMover: CompanionObjectMover

    @Before
    fun setup() {
        companionObjectMover = CompanionObjectMover(KtElementBoundFinder())
    }

    @Test
    fun replace() {
        val file =
            """class Sample {

                companion object {
                    private const val AA = "123"
                }

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val companionObjectFindResult = KtCompanionFinder().findCompanionObjects(ktFile)
        val barFunction = requireNotNull(KtFunctionFinder("bar").also { ktFile.accept(it) }.function)
        val companion = companionObjectFindResult.companionObjects.first()

        val result = companionObjectMover.moveCompanionObject(
            ktFile = ktFile,
            companionObject = companion.companionObject,
            moveAfterElement = barFunction
        )

        println(result)

        val expectingResult =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                companion object {
                    private const val AA = "123"
                }
            }
            """
        assertThat(result, IsEqual(expectingResult))
    }
}