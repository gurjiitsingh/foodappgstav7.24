package com.it10x.foodappgstav7_24.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// =====================================================
// THEME MODES
// =====================================================

enum class PosThemeMode {
    AUTO,
    CLASSIC,
    MIDNIGHT,
    GSTA,
    OCEAN,
    SLATE,
    LATTE
}
// =====================================================
// COLOR DATA CLASSES
// =====================================================

data class PosAccentColors(
    val cartAddBg: Color,
    val cartAddText: Color,
    val cartRemoveBorder: Color,
    val cartRemoveText: Color,

    val primaryActionBg: Color,
    val primaryActionText: Color
)

data class PosProductColors(
    val productCardBg: Color,
    val productCardText: Color
)

data class PosTopBarColors(
    val background: Color,
    val content: Color
)

data class PosCategoryColors(
    val selectedBg: Color,
    val selectedText: Color,
    val unselectedBg: Color,
    val unselectedText: Color
)

data class PosTopBarButtonColors(
    val selectedBg: Color,
    val selectedContent: Color,
    val unselectedBg: Color,
    val unselectedContent: Color
)

data class PosBillColors(
    val billBg: Color,
    val billText: Color,
    val billTab: Color,

    // Input fields
    val inputBg: Color,
    val inputBorder: Color,
    val inputActiveBg: Color,
    val inputActiveBorder: Color,

    // Common actions
    val success: Color,
    val warning: Color,
    val danger: Color
)

// =====================================================
// GLOBAL ACCESS
// =====================================================

object PosTheme {
    lateinit var accent: PosAccentColors
        internal set
    lateinit var product: PosProductColors
        internal set
    lateinit var topBar: PosTopBarColors
        internal set
    lateinit var category: PosCategoryColors
        internal set
    lateinit var topBarButton: PosTopBarButtonColors
        internal set

    lateinit var bill: PosBillColors
        internal set
}

// =====================================================
// MAIN THEME
// =====================================================

@Composable
fun FoodPosTheme(
    mode: PosThemeMode = PosThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()

    val finalMode = when (mode) {
        PosThemeMode.AUTO -> if (isSystemDark) PosThemeMode.MIDNIGHT else PosThemeMode.CLASSIC
        else -> mode
    }

    val colorScheme = when (finalMode) {



        PosThemeMode.CLASSIC -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFFB8500),       // Strong orange (CTA)
                cartAddText = Color.White,

                cartRemoveBorder = Color(0xFF219EBC), // Blue border
                cartRemoveText = Color(0xFF219EBC),

                primaryActionBg = Color(0xFF219EBC),  // Main action (blue)
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF023047)   // Deep navy text
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF023047),       // Premium dark header
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFFFB703),       // Yellow highlight
                selectedText = Color(0xFF023047),

                unselectedBg = Color(0xFF8ECAE6),     // Soft blue
                unselectedText = Color(0xFF023047)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFF219EBC),
                selectedContent = Color.White,

                unselectedBg = Color(0xFF8ECAE6),
                unselectedContent = Color(0xFF023047)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF023047),
                billTab = Color(0xFFF8FAFC),

                inputBg = Color(0xFFF8FAFC),
                inputBorder = Color(0xFFB0BEC5),
                inputActiveBg = Color(0xFFE3F2FD),
                inputActiveBorder = Color(0xFF219EBC),

                success = Color(0xFF2E7D32),
                warning = Color(0xFFFB8500),
                danger = Color(0xFFC62828)
            )

            lightColorScheme(
                primary = Color(0xFF219EBC),
                onPrimary = Color.White,

                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF023047),

                surface = Color.White,
                onSurface = Color(0xFF023047),

                error = Color(0xFFDC2626)
            )


        }
        // ================= DARK =================
        PosThemeMode.MIDNIGHT -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFF97316),
                cartAddText = Color.White,
                cartRemoveBorder = Color(0xFFF97316),
                cartRemoveText = Color(0xFFF97316),

                primaryActionBg = Color(0xFFF97316),
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color(0xFF1E293B),
                productCardText = Color.White
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF1E293B),
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFF97316),
                selectedText = Color.White,
                unselectedBg = Color(0xFF334155),
                unselectedText = Color.White.copy(alpha = 0.7f)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFF97316),
                selectedContent = Color.White,
                unselectedBg = Color.White.copy(alpha = 0.08f),
                unselectedContent = Color.White.copy(alpha = 0.7f)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color(0xFF1E293B),
                billText = Color.White,
                billTab = Color(0xFF0F172A),

                inputBg = Color(0xFF273548),
                inputBorder = Color(0xFF475569),
                inputActiveBg = Color(0xFF334155),
                inputActiveBorder = Color(0xFFF97316),

                success = Color(0xFF22C55E),
                warning = Color(0xFFF97316),
                danger = Color(0xFFEF4444)
            )

            darkColorScheme(
                primary = Color(0xFFF97316),
                onPrimary = Color.White,
                background = Color(0xFF0F172A),
                onBackground = Color.White,
                surface = Color(0xFF1E293B),
                onSurface = Color.White,
                error = Color(0xFFDC2626)
            )

        }

        // ================= GSTA =================
        PosThemeMode.GSTA -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFFACC15),       // Premium yellow
                cartAddText = Color(0xFF111827),

                cartRemoveBorder = Color(0xFFEF4444), // Red
                cartRemoveText = Color(0xFFEF4444),

                primaryActionBg = Color(0xFFFACC15), // Main POS buttons
                primaryActionText = Color(0xFF111827)
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color(0xFF1F2937),    // Dark card
                productCardText = Color.White
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF111827),       // Dark header
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFFACC15),       // Yellow selected
                selectedText = Color(0xFF111827),

                unselectedBg = Color(0xFF1F2937),
                unselectedText = Color.White.copy(alpha = 0.75f)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFFACC15),
                selectedContent = Color(0xFF111827),

                unselectedBg = Color.White.copy(alpha = 0.08f),
                unselectedContent = Color.White.copy(alpha = 0.85f)
            )
            PosTheme.bill = PosBillColors(
                billBg = Color(0xFF1F2937),
                billText = Color.White,
                billTab = Color(0xFF111827),

                inputBg = Color(0xFF27303D),
                inputBorder = Color(0xFF4B5563),
                inputActiveBg = Color(0xFF374151),
                inputActiveBorder = Color(0xFFFACC15),

                success = Color(0xFF22C55E),
                warning = Color(0xFFFACC15),
                danger = Color(0xFFEF4444)
            )

            darkColorScheme(
                primary = Color(0xFFFACC15),          // Yellow
                onPrimary = Color(0xFF111827),

                background = Color(0xFF0B1120),       // Deep dark
                onBackground = Color.White,

                surface = Color(0xFF1F2937),
                onSurface = Color.White,

                error = Color(0xFFEF4444)             // Red
            )

        }


        // ================= OCEAN BLUE=================
        PosThemeMode.OCEAN -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFF2C438),        // Yellow CTA (attention)
                cartAddText = Color(0xFF035AA6),

                cartRemoveBorder = Color(0xFF049DD9),
                cartRemoveText = Color(0xFF049DD9),

                primaryActionBg = Color(0xFF049DD9),  // Main blue action
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF035AA6)   // Deep blue text
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF035AA6),       // Strong brand header
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFF2C438),       // Yellow highlight
                selectedText = Color(0xFF035AA6),

                unselectedBg = Color(0xFFF2F2F2),     // Soft neutral
                unselectedText = Color(0xFF035AA6)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFF04B2D9),       // Slightly lighter blue
                selectedContent = Color.White,

                unselectedBg = Color.Transparent,
                unselectedContent = Color.White.copy(alpha = 0.85f)
            )
            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF035AA6),
                billTab = Color(0xFFF2F2F2),

                inputBg = Color(0xFFF8FAFC),
                inputBorder = Color(0xFFBFD6E5),
                inputActiveBg = Color(0xFFE3F2FD),
                inputActiveBorder = Color(0xFF049DD9),

                success = Color(0xFF2E7D32),
                warning = Color(0xFFF2C438),
                danger = Color(0xFFC62828)
            )

            lightColorScheme(
                primary = Color(0xFF049DD9),
                onPrimary = Color.White,

                background = Color(0xFFF2F2F2),       // Soft app background
                onBackground = Color(0xFF035AA6),

                surface = Color.White,
                onSurface = Color(0xFF035AA6),

                error = Color(0xFFDC2626)
            )

        }

        // ================= SLATE BLUESLATE =================
        PosThemeMode.SLATE -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFEE6C4D),       // Coral action
                cartAddText = Color.White,

                cartRemoveBorder = Color(0xFFEE6C4D),
                cartRemoveText = Color(0xFFEE6C4D),

                primaryActionBg = Color(0xFFEE6C4D),
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
              //  productCardBg = Color(0xFFE0FBFC),
                productCardBg =   Color(0xFF3D5A80),
                productCardText = Color.White,
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFF3D5A80),
                content = Color.White
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFEE6C4D),
                selectedText = Color.White,

                unselectedBg = Color(0xFF98C1D9),
                unselectedText = Color(0xFF293241)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFEE6C4D),
                selectedContent = Color.White,

                unselectedBg = Color(0xFF98C1D9),
                unselectedContent = Color(0xFF293241)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color(0xFFE0FBFC),
                billText = Color(0xFF293241),
                billTab = Color(0xFF3D5A80),

                inputBg = Color.White,
                inputBorder = Color(0xFF98C1D9),
                inputActiveBg = Color(0xFFF4FDFF),
                inputActiveBorder = Color(0xFFEE6C4D),

                success = Color(0xFF2E7D32),
                warning = Color(0xFFEE6C4D),
                danger = Color(0xFFC62828)
            )
            lightColorScheme(
                primary = Color(0xFFEE6C4D),
                onPrimary = Color.White,

                background = Color(0xFF3D5A80),
                onBackground = Color.White,

                surface = Color(0xFFE0FBFC),
                onSurface = Color(0xFF293241),

                error = Color(0xFFDC2626)
            )

        }

        // ================= LATTE combination of light coffee=================
        PosThemeMode.LATTE -> {

            PosTheme.accent = PosAccentColors(
                cartAddBg = Color(0xFFE8CCC5),       // warm soft peach (CTA)
                cartAddText = Color(0xFF1E293B),

                cartRemoveBorder = Color(0xFF807E79),
                cartRemoveText = Color(0xFF807E79),

                primaryActionBg = Color(0xFF807E79), // main brand action
                primaryActionText = Color.White
            )

            PosTheme.product = PosProductColors(
                productCardBg = Color.White,
                productCardText = Color(0xFF1E293B)
            )

            PosTheme.topBar = PosTopBarColors(
                background = Color(0xFFE3E3E3),      // soft neutral header
                content = Color(0xFF1E293B)
            )

            PosTheme.category = PosCategoryColors(
                selectedBg = Color(0xFFCFD4AE),
                selectedText = Color(0xFF1E293B),

                unselectedBg = Color(0xFFF6E7C6),
                unselectedText = Color(0xFF807E79)
            )

            PosTheme.topBarButton = PosTopBarButtonColors(
                selectedBg = Color(0xFFBDCED3),
                selectedContent = Color(0xFF1E293B),

                unselectedBg = Color.Transparent,
                unselectedContent = Color(0xFF807E79)
            )

            PosTheme.bill = PosBillColors(
                billBg = Color.White,
                billText = Color(0xFF1E293B),
                billTab = Color(0xFFE3E3E3),

                inputBg = Color(0xFFFAFAFA),
                inputBorder = Color(0xFFD6D3D1),
                inputActiveBg = Color(0xFFF6F3EF),
                inputActiveBorder = Color(0xFF807E79),

                success = Color(0xFF4CAF50),
                warning = Color(0xFFE8CCC5),
                danger = Color(0xFFD32F2F)
            )
            lightColorScheme(
                primary = Color(0xFF807E79),
                onPrimary = Color.White,

                background = Color.White,            // ✅ changed to pure white
                onBackground = Color(0xFF1E293B),

                surface = Color.White,
                onSurface = Color(0xFF1E293B),

                error = Color(0xFFDC2626)
            )

        }


        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}