package br.com.zup.pix.chave

import br.com.zup.pix.servicosExternos.KeyType

enum class TipoChavePix {
    UNKNOWN_CHAVE_PIX {
        override fun validaChave(chave: String): Boolean {
            return false
        }

        override fun toBbcKeyType(): KeyType {
            return KeyType.UNKNOWN
        }
    },
    EMAIL {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+\$".toRegex())
        }

        override fun toBbcKeyType(): KeyType {
            return KeyType.EMAIL
        }
    },
    CELULAR {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9]{11}\$".toRegex())
        }

        override fun toBbcKeyType(): KeyType {
            return KeyType.PHONE
        }
    },
    CPF {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9]{11}\$".toRegex())
        }

        override fun toBbcKeyType(): KeyType {
            return KeyType.CPF
        }
    },
    CHAVE_ALEATORIA {
        override fun validaChave(chave: String): Boolean {
            return chave.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$".toRegex())
        }

        override fun toBbcKeyType(): KeyType {
            return KeyType.RANDOM
        }
    };
    abstract fun validaChave(chave: String): Boolean
    abstract fun toBbcKeyType(): KeyType

}
