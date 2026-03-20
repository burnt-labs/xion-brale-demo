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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a,\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\u0018\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0003\u001a,\u0010\u000b\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\f\u001a\u00020\rH\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u000e\u0010\u000f\u001a&\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\b\u0010\u0014\u001a\u00020\u0001H\u0003\u001a\u001e\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00122\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u001e\u0010\u0017\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0018"}, d2 = {"ConfirmContent", "", "uiState", "Lcom/burnt/xiondemo/ui/screens/send/SendUiState;", "onCancel", "Lkotlin/Function0;", "onConfirm", "ConfirmRow", "label", "", "value", "DetailRow", "valueColor", "Landroidx/compose/ui/graphics/Color;", "DetailRow-mxwnekA", "(Ljava/lang/String;Ljava/lang/String;J)V", "FormContent", "viewModel", "Lcom/burnt/xiondemo/ui/screens/send/SendViewModel;", "onReview", "LoadingContent", "SendSheetContent", "onDone", "SuccessContent", "app_debug"})
public final class SendScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void SendSheetContent(@org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.ui.screens.send.SendViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void FormContent(com.burnt.xiondemo.ui.screens.send.SendUiState uiState, com.burnt.xiondemo.ui.screens.send.SendViewModel viewModel, kotlin.jvm.functions.Function0<kotlin.Unit> onReview) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConfirmContent(com.burnt.xiondemo.ui.screens.send.SendUiState uiState, kotlin.jvm.functions.Function0<kotlin.Unit> onCancel, kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LoadingContent() {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SuccessContent(com.burnt.xiondemo.ui.screens.send.SendUiState uiState, kotlin.jvm.functions.Function0<kotlin.Unit> onDone) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void ConfirmRow(java.lang.String label, java.lang.String value) {
    }
}