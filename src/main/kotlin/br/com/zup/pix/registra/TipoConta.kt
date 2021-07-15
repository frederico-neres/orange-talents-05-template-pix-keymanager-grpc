package br.com.zup.pix.registra

import br.com.zup.pix.servicosExternos.AccountType

enum class TipoConta {
    UNKNOWN_CONTA {
        override fun toBbcAccountType(): AccountType {
            return AccountType.UNKNOWN
        }
    },
    CONTA_CORRENTE {
        override fun toBbcAccountType(): AccountType {
            return AccountType.CACC
        }
    },
    CONTA_POUPANCA {
        override fun toBbcAccountType(): AccountType {
            return AccountType.SVGS
        }
    };

    abstract fun toBbcAccountType(): AccountType
}
