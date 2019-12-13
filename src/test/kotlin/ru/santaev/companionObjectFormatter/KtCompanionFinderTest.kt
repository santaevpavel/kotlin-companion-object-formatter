package ru.santaev.companionObjectFormatter

import org.hamcrest.collection.IsCollectionWithSize
import org.hamcrest.collection.IsEmptyCollection
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class KtCompanionFinderTest {

    private lateinit var finder: KtCompanionObjectFinder

    @Before
    fun setUp() {
        finder = KtCompanionObjectFinder()
    }

    @Test
    fun `should find companion object on class top`() {
        val file =
            """class ru.santaev.companionObjectFormatter.Sample {

                companion object {
                    private const val AAAA = 0
                }

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsCollectionWithSize(IsEqual(1)))
    }

    @Test
    fun `should find companion object on class bottom`() {
        val file =
            """class ru.santaev.companionObjectFormatter.Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                companion object {
                    private const val AAAA = 0
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsCollectionWithSize(IsEqual(1)))
    }

    @Test
    fun `should not find companion object`() {
        val file =
            """class ru.santaev.companionObjectFormatter.Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsEmptyCollection())
    }

    @Test
    fun `should not find object`() {
        val file =
            """class ru.santaev.companionObjectFormatter.Sample {

                fun foo(): Int = 0

                private fun bar(): Int {
                    return object: IInterface {
                        fun function() = 2
                    }
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsEmptyCollection())
    }

    @Test
    fun `should find two companion object`() {
        val file =
            """class ru.santaev.companionObjectFormatter.Sample {

                companion object {
                    private val AA = "asd"
                }

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                class Inner {
                    companion object {
                        private val AA = "asd"
                    }
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val result = finder.findCompanionObjects(ktFile)

        assertThat(result.companionObjects, IsCollectionWithSize(IsEqual(2)))
    }
}