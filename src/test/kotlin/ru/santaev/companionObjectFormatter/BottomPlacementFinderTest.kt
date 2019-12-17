package ru.santaev.companionObjectFormatter

import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import ru.santaev.companionObjectFormatter.placementFinder.IKtCompanionObjectPlacementFinder.Placement
import ru.santaev.companionObjectFormatter.placementFinder.PlacementAtBottomOfClassFinder
import ru.santaev.companionObjectFormatter.utils.KtClassFinder
import ru.santaev.companionObjectFormatter.utils.KtFunctionFinder
import ru.santaev.companionObjectFormatter.utils.KtSecondaryConstructorFinder

class BottomPlacementFinderTest {

    private lateinit var placementFinder: PlacementAtBottomOfClassFinder

    @Before
    fun setUp() {
        placementFinder = PlacementAtBottomOfClassFinder()
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
        val ktFile = KtFileParser().parseString(file)
        val ktClass = KtClassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals((result as Placement.AfterElement).element, barFunction)
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
        val ktFile = KtFileParser().parseString(file)
        val ktClass = KtClassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals((result as Placement.AfterElement).element, barFunction)
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
        val ktFile = KtFileParser().parseString(file)

        val ktClass = KtClassFinder("Sample").also { ktFile.accept(it) }.klass
        val barFunction = KtFunctionFinder("bar").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals((result as Placement.AfterElement).element, barFunction)
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
        val ktFile = KtFileParser().parseString(file)
        val ktClass = KtClassFinder("Inner").also { ktFile.accept(it) }.klass
        val fooInnerFunction = KtFunctionFinder("fooInner").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals((result as Placement.AfterElement).element, fooInnerFunction)
    }

    @Test
    fun `should find below constructors`() {
        val file =
            """class Sample {

                companion object {
                    private const val A = "a"
                }

                fun foo(): Int = 0

                private fun bar(): Int {
                    return 0
                }

                constructor() {
                    val b = 0
                }

                constructor(arg: Int) {
                    val b = arg
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val ktClass = KtClassFinder("Sample").also { ktFile.accept(it) }.klass
        val constructor = KtSecondaryConstructorFinder().also { ktFile.accept(it) }.constructor

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals(constructor, (result as Placement.AfterElement).element)
    }

    @Test
    fun `should find above inner class and below function`() {
        val file =
            """class Sample {

                fun foo(): Int = 0

                inner class A {

                    fun bar() = 0
                }

                companion object {
                    private const val A = "a"
                }
            }
            """
        val ktFile = KtFileParser().parseString(file)
        val ktClass = KtClassFinder("Sample").also { ktFile.accept(it) }.klass
        val function = KtFunctionFinder("foo").also { ktFile.accept(it) }.function

        val result = placementFinder.findCompanionObjectNewPlacementLine(ktFile, ktClass!!)

        assertThat(result, IsInstanceOf(Placement.AfterElement::class.java))
        assertEquals(function, (result as Placement.AfterElement).element)
    }
}
