class Sample {

    fun foo(): Int = 0

    private fun bar(): Int {
        return 0
    }

    companion object {
        private const val AAAA = 0
    }

    class Inner {

        val a = 0

        companion object {
            private const val AAAA = 0
        }
    }

    enum class EnumClass {
        FIRST,
        SECOND;

        val a = 0

        companion object {
            private const val AAAA = 0

            fun func() {

            }
        }
    }
}

