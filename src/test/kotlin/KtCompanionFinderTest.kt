import org.hamcrest.collection.IsCollectionWithSize
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class KtCompanionFinderTest {

    private lateinit var finder: KtCompanionFinder

    @Before
    fun setUp() {
        finder = KtCompanionFinder()
    }

    @Test
    fun `should find companion object on class top`() {
        val file =
            """class Sample {

                companion object {
                    private const val AAAA = 0
                }

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsCollectionWithSize(IsEqual(1)))
    }

    @Test
    fun `should find companion object on class bottom`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                companion object {
                    private const val AAAA = 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsCollectionWithSize(IsEqual(1)))
    }

    @Test
    fun `should not find companion object`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileReader().fromString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsEmptyCollection())
    }
}