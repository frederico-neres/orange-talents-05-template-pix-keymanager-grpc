package br.com.zup.pix.servicosExternos

import br.com.zup.pix.chave.TipoChavePix

enum class KeyType {
    CPF {
        override fun toTipoChavePix(): TipoChavePix {
            return TipoChavePix.CPF
        }
    },
    RANDOM {
        override fun toTipoChavePix(): TipoChavePix {
            return TipoChavePix.CHAVE_ALEATORIA
        }
    },
    EMAIL {
        override fun toTipoChavePix(): TipoChavePix {
            return TipoChavePix.EMAIL
        }
    },
    PHONE {
        override fun toTipoChavePix(): TipoChavePix {
            return TipoChavePix.CELULAR
        }
    },
    UNKNOWN {
        override fun toTipoChavePix(): TipoChavePix {
            return TipoChavePix.UNKNOWN_CHAVE_PIX
        }
    };

    abstract fun toTipoChavePix(): TipoChavePix

}