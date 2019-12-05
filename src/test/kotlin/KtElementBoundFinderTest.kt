import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import utils.KtFunctionFinder

class KtElementBoundFinderTest {

    private lateinit var finder: KtElementBoundFinder

    @Before
    fun setUp() {
        finder = KtElementBoundFinder()
    }

    @Test
    fun `should find bounds of function`() {
        val file =
            """class Sample {

                fun foo(): Int {
                    val i = 0
                    return 1
                }

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val fooFunction = KtFunctionFinder("foo").also { ktFile.accept(it) }.function

        val bounds = finder.find(ktFile, fooFunction!!)

        Assert.assertThat(bounds.startLine, equalTo(3))
        Assert.assertThat(bounds.endLine, equalTo(6))
    }

    @Test
    fun `should find bounds of one line function`() {
        val file =
            """class Sample {

                fun foo(): Int {
                    val i = 0
                    return 1
                }

                private fun bar() = 3
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val fooFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val bounds = finder.find(ktFile, fooFunction!!)

        Assert.assertThat(bounds.startLine, equalTo(8))
        Assert.assertThat(bounds.endLine, equalTo(8))
    }

    @Test
    fun `should find bounds of expression function`() {
        val file =
            """class Sample {

                fun foo(): Int {
                    val i = 0
                    return 1
                }

                private fun bar() = "AAA".map { char ->
                    char + char
                }
                    .filter { it != 'B' }

            }
            """
        val ktFile = KtFileReader().fromString(file)
        val fooFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val bounds = finder.find(ktFile, fooFunction!!)

        Assert.assertThat(bounds.startLine, equalTo(8))
        Assert.assertThat(bounds.endLine, equalTo(11))
    }

}