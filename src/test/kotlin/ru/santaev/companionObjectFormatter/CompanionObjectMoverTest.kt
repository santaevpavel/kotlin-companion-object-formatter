package ru.santaev.companionObjectFormatter

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.Before
import org.junit.Test
import ru.santaev.companionObjectFormatter.utils.KtFunctionFinder

class CompanionObjectMoverTest {

    private lateinit var companionObjectMover: KtCompanionObjectMover

    @Before
    fun setup() {
        companionObjectMover =
            KtCompanionObjectMover(KtElementBoundFinder())
    }

    @Test
    fun `should move companion after specified function`() {
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
        val ktFile = KtFileParser().parseString(file)
        val companionObjectFindResult = KtCompanionObjectFinder().findCompanionObjects(ktFile)
        val barFunction = requireNotNull(KtFunctionFinder("bar").also { ktFile.accept(it) }.function)
        val companion = companionObjectFindResult.companionObjects.first()

        val result = companionObjectMover.moveCompanionObject(
            ktFile = ktFile,
            companionObject = companion.companionObject,
            tallElement = barFunction
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

    @Test
    fun `should move companion after specified function in inner class`() {
        val file =
            """class Sample {

                class Inner {
                    companion object {
                        private const val AA = "123"
                    }

                    fun foo(): Int = 0
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val companionObjectFindResult = KtCompanionObjectFinder().findCompanionObjects(ktFile)
        val barFunction = requireNotNull(KtFunctionFinder("foo").also { ktFile.accept(it) }.function)
        val companion = companionObjectFindResult.companionObjects.first()

        val result = companionObjectMover.moveCompanionObject(
            ktFile = ktFile,
            companionObject = companion.companionObject,
            tallElement = barFunction
        )

        println(result)

        val expectingResult =
            """class Sample {

                class Inner {

                    fun foo(): Int = 0

                    companion object {
                        private const val AA = "123"
                    }
                }
            }
            """
        assertThat(result, IsEqual(expectingResult))
    }

    @Test
    fun `should move up companion object`() {
        val file =
            """class Sample {

                    fun bar(): Int = 0

                    fun foo(): Int = 0

                    companion object {
                        private const val AA = "123"
                    }
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val companionObjectFindResult = KtCompanionObjectFinder().findCompanionObjects(ktFile)
        val barFunction = requireNotNull(KtFunctionFinder("bar").also { ktFile.accept(it) }.function)
        val companion = companionObjectFindResult.companionObjects.first()

        val result = companionObjectMover.moveCompanionObject(
            ktFile = ktFile,
            companionObject = companion.companionObject,
            tallElement = barFunction
        )

        println(result)

        val expectingResult =
            """class Sample {

                    fun bar(): Int = 0

                    companion object {
                        private const val AA = "123"
                    }

                    fun foo(): Int = 0

                }
            }
            """
        assertThat(result, IsEqual(expectingResult))
    }
}
