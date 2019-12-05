class Sample {

    companion object {
        private const val AAAA = 0
    }

    fun foo(): Int = 0

    private fun bar(): Int {
        return 0
    }

    class Inner {
        companion object {
            private const val AAAA = 0
        }

        val a = 0
    }

    enum class EnumClass {
        FIRST,
        SECOND;

        companion object {
            private const val AAAA = 0

            fun func() {

            }
        }

        val a = 0
    }
}

