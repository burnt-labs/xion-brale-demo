package com.burnt.xiondemo.ui.screens.send;

import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.KeyboardType;
import androidx.compose.ui.text.style.TextOverflow;
import java.text.NumberFormat;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/burnt/xiondemo/ui/screens/send/SendStep;", "", "(Ljava/lang/String;I)V", "FORM", "CONFIRM", "LOADING", "SUCCESS", "app_debug"})
enum SendStep {
    /*public static final*/ FORM /* = new FORM() */,
    /*public static final*/ CONFIRM /* = new CONFIRM() */,
    /*public static final*/ LOADING /* = new LOADING() */,
    /*public static final*/ SUCCESS /* = new SUCCESS() */;
    
    SendStep() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.burnt.xiondemo.ui.screens.send.SendStep> getEntries() {
        return null;
    }
}