package com.it10x.foodappgstav7_24.fiskaly

import com.it10x.foodappgstav7_24.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_24.fiscal.FiscalContext
import com.it10x.foodappgstav7_24.fiscal.FiscalService
import com.it10x.foodappgstav7_24.ui.payment.PaymentInput

class SpainFiscalService : FiscalService {

    override suspend fun start(): FiscalContext {
        return FiscalContext(null, null)
    }

    override suspend fun finish(
        context: FiscalContext,
        payments: List<PaymentInput>,
        items: List<PosKotItemEntity>
    ) {
        // TODO: implement SIGN ES later
    }

    override suspend fun cancel(context: FiscalContext) {
        // TODO
    }
}