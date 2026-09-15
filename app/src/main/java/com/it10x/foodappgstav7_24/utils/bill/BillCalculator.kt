package com.it10x.foodappgstav7_24.utils.bill


import com.it10x.foodappgstav7_24.data.pos.entities.PosKotItemEntity
//import com.it10x.foodappgstav7_24.utils.ModifierJsonHelper
import com.it10x.foodappgstav7_24.utils.MoneyUtils
import com.it10x.foodappgstav7_24.utils.tax.TaxMode
import kotlin.math.roundToLong


object BillCalculator {

    fun calculate(
        items: List<PosKotItemEntity>,
        taxMode: String,
        discountFlat: Double,
        discountPercent: Double,
        deliveryFee: Double,
        deliveryTaxPercent: Double
    ): BillCalculationResult {

        // =========================
        // ITEM SUBTOTAL
        // =========================
        val itemSubtotalPaise = items.sumOf {

//            val modifierPrice =
//                ModifierJsonHelper.fromJson(it.modifiersJson)
//                    .flatMap { g -> g.items }
//                    .sumOf { m -> m.price }

          //  val base = it.basePrice + modifierPrice
            val base = it.basePrice

            MoneyUtils.toPaise(base) * it.quantity
        }

        // =========================
        // RAW TAX
        // =========================
        var exclusiveTaxPaise = 0L
        var inclusiveTaxPaise = 0L

        items.forEach { item ->

            val basePaise = MoneyUtils.toPaise(item.basePrice)

            val taxType =
                effectiveTaxType(
                    taxMode,
                    item.taxType
                )

            val taxPerItem = when (taxType.lowercase()) {

                "exclusive" -> {
                    (basePaise * item.taxRate / 100.0)
                        .roundToLong()
                }

                "inclusive" -> {
                    (basePaise * item.taxRate / (100.0 + item.taxRate))
                        .roundToLong()
                }

                else -> 0L
            }

            if (taxType.equals("exclusive", true)) {
                exclusiveTaxPaise += taxPerItem * item.quantity
            } else if (taxType.equals("inclusive", true)) {
                inclusiveTaxPaise += taxPerItem * item.quantity
            }
        }

        // =========================
        // DISCOUNT
        // =========================
        val flatPaise =
            MoneyUtils.toPaise(discountFlat)

        val percentPaise =
            ((itemSubtotalPaise * discountPercent) / 100.0)
                .roundToLong()

        val discountPaise =
            if (flatPaise > 0)
                flatPaise
            else
                percentPaise

        val safeDiscountPaise =
            discountPaise.coerceAtMost(itemSubtotalPaise)

        val discountRatio =
            if (itemSubtotalPaise == 0L)
                0.0
            else
                safeDiscountPaise.toDouble() /
                        itemSubtotalPaise.toDouble()

        exclusiveTaxPaise =
            (exclusiveTaxPaise * (1 - discountRatio))
                .roundToLong()

        inclusiveTaxPaise =
            (inclusiveTaxPaise * (1 - discountRatio))
                .roundToLong()

        // =========================
        // DELIVERY
        // =========================
        val deliveryFeePaise =
            MoneyUtils.toPaise(deliveryFee)

        val deliveryRate = resolveDeliveryTaxRate(
            items,
            taxMode,
            deliveryTaxPercent
        )
        val deliveryTaxPaise =
            (deliveryFeePaise * deliveryRate / 100.0)
                .roundToLong()
        val totalTaxPaise =
            exclusiveTaxPaise +
                    inclusiveTaxPaise +
                    deliveryTaxPaise

        // =========================
// TAXABLE AMOUNT
// =========================
// Taxable amount after discount,
// before adding GST.
        val taxableAmountPaise =
            itemSubtotalPaise - safeDiscountPaise

// =========================
// GRAND TOTAL
// =========================
        val hasExclusiveTax = items.any {
            effectiveTaxType(taxMode, it.taxType).equals("exclusive", ignoreCase = true)
        }

        val calculatedGrandTotalPaise =
            itemSubtotalPaise -
                    safeDiscountPaise +
                    exclusiveTaxPaise +
                    deliveryFeePaise +
                    deliveryTaxPaise

// =========================
// ROUND OFF
// =========================
// Round payable amount to nearest rupee.
        val roundedGrandTotalPaise =
            ((calculatedGrandTotalPaise + 50L) / 100L) * 100L

        val roundOffPaise =
            roundedGrandTotalPaise -
                    calculatedGrandTotalPaise

        val grandTotalPaise =
            calculatedGrandTotalPaise +
                    roundOffPaise

        return BillCalculationResult(
            itemSubtotalPaise = itemSubtotalPaise,

            exclusiveTaxPaise = exclusiveTaxPaise,
            inclusiveTaxPaise = inclusiveTaxPaise,
            totalTaxPaise = totalTaxPaise,

            discountPaise = safeDiscountPaise,

            deliveryFeePaise = deliveryFeePaise,
            deliveryTaxPaise = deliveryTaxPaise,

            taxableAmountPaise = taxableAmountPaise,
            roundOffPaise = roundOffPaise,

            grandTotalPaise = grandTotalPaise
        )
    }

    private fun resolveDeliveryTaxRate(
        items: List<PosKotItemEntity>,
        taxMode: String,
        deliveryTaxPercent: Double
    ): Double {

        // 1. Explicit delivery GST configured
        if (deliveryTaxPercent > 0) {
            return deliveryTaxPercent
        }

        // 2. Otherwise use item/order GST
        val firstItem = items.firstOrNull() ?: return 0.0

        return firstItem.taxRate
    }


}

private fun effectiveTaxType(
    outletTaxMode: String,
    itemTaxType: String
): String {

    return when (outletTaxMode) {

        TaxMode.FORCE_INCLUSIVE -> "inclusive"

        TaxMode.FORCE_EXCLUSIVE -> "exclusive"

        else -> itemTaxType
    }
}