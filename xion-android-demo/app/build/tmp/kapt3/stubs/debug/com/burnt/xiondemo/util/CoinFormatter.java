package com.burnt.xiondemo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006J\u000e\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0006J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006J\u000e\u0010\r\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/burnt/xiondemo/util/CoinFormatter;", "", "()V", "displayFormat", "Ljava/text/DecimalFormat;", "displayToMicro", "", "displayAmount", "formatWithDenom", "microAmount", "isValidAmount", "", "amount", "microToDisplay", "app_debug"})
public final class CoinFormatter {
    @org.jetbrains.annotations.NotNull()
    private static final java.text.DecimalFormat displayFormat = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.burnt.xiondemo.util.CoinFormatter INSTANCE = null;
    
    private CoinFormatter() {
        super();
    }
    
    /**
     * Convert micro-denomination (uxion) to display denomination (XION).
     * 1 XION = 1,000,000 uxion
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String microToDisplay(@org.jetbrains.annotations.NotNull()
    java.lang.String microAmount) {
        return null;
    }
    
    /**
     * Convert display denomination (XION) to micro-denomination (uxion).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String displayToMicro(@org.jetbrains.annotations.NotNull()
    java.lang.String displayAmount) {
        return null;
    }
    
    /**
     * Format a micro amount with denom for display: "1.5 XION"
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatWithDenom(@org.jetbrains.annotations.NotNull()
    java.lang.String microAmount) {
        return null;
    }
    
    /**
     * Validate that an amount string is a valid positive number.
     */
    public final boolean isValidAmount(@org.jetbrains.annotations.NotNull()
    java.lang.String amount) {
        return false;
    }
}