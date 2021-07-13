package br.com.zup.pix.registra

enum class TipoChavePix {
    UNKNOWN_CHAVE_PIX {
        override fun validaChave(chave: String): Boolean {
            return false
        }
    },
    EMAIL {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+\$".toRegex())
        }
    },
    CELULAR {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    CPF {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    CHAVE_ALEATORIA {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$".toRegex())
        }
    };
    abstract fun validaChave(chave: String): Boolean
}
