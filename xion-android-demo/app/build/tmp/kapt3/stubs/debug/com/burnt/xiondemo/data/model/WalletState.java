package com.burnt.xiondemo.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0003\u0003\u0004\u0005B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0003\u0006\u0007\b\u00a8\u0006\t"}, d2 = {"Lcom/burnt/xiondemo/data/model/WalletState;", "", "()V", "Connected", "Connecting", "Disconnected", "Lcom/burnt/xiondemo/data/model/WalletState$Connected;", "Lcom/burnt/xiondemo/data/model/WalletState$Connecting;", "Lcom/burnt/xiondemo/data/model/WalletState$Disconnected;", "app_debug"})
public abstract class WalletState {
    
    private WalletState() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0011\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\tH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u00072\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u00d6\u0003J\t\u0010\u001c\u001a\u00020\u001dH\u00d6\u0001J\t\u0010\u001e\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000e\u00a8\u0006\u001f"}, d2 = {"Lcom/burnt/xiondemo/data/model/WalletState$Connected;", "Lcom/burnt/xiondemo/data/model/WalletState;", "metaAccountAddress", "", "sessionKeyAddress", "treasuryAddress", "grantsActive", "", "sessionExpiresAt", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZJ)V", "getGrantsActive", "()Z", "getMetaAccountAddress", "()Ljava/lang/String;", "getSessionExpiresAt", "()J", "getSessionKeyAddress", "getTreasuryAddress", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "other", "", "hashCode", "", "toString", "app_debug"})
    public static final class Connected extends com.burnt.xiondemo.data.model.WalletState {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String metaAccountAddress = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String sessionKeyAddress = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String treasuryAddress = null;
        private final boolean grantsActive = false;
        private final long sessionExpiresAt = 0L;
        
        public Connected(@org.jetbrains.annotations.NotNull()
        java.lang.String metaAccountAddress, @org.jetbrains.annotations.NotNull()
        java.lang.String sessionKeyAddress, @org.jetbrains.annotations.NotNull()
        java.lang.String treasuryAddress, boolean grantsActive, long sessionExpiresAt) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMetaAccountAddress() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getSessionKeyAddress() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTreasuryAddress() {
            return null;
        }
        
        public final boolean getGrantsActive() {
            return false;
        }
        
        public final long getSessionExpiresAt() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        public final boolean component4() {
            return false;
        }
        
        public final long component5() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.burnt.xiondemo.data.model.WalletState.Connected copy(@org.jetbrains.annotations.NotNull()
        java.lang.String metaAccountAddress, @org.jetbrains.annotations.NotNull()
        java.lang.String sessionKeyAddress, @org.jetbrains.annotations.NotNull()
        java.lang.String treasuryAddress, boolean grantsActive, long sessionExpiresAt) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/burnt/xiondemo/data/model/WalletState$Connecting;", "Lcom/burnt/xiondemo/data/model/WalletState;", "step", "Lcom/burnt/xiondemo/data/model/ConnectionStep;", "(Lcom/burnt/xiondemo/data/model/ConnectionStep;)V", "getStep", "()Lcom/burnt/xiondemo/data/model/ConnectionStep;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Connecting extends com.burnt.xiondemo.data.model.WalletState {
        @org.jetbrains.annotations.NotNull()
        private final com.burnt.xiondemo.data.model.ConnectionStep step = null;
        
        public Connecting(@org.jetbrains.annotations.NotNull()
        com.burnt.xiondemo.data.model.ConnectionStep step) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.burnt.xiondemo.data.model.ConnectionStep getStep() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.burnt.xiondemo.data.model.ConnectionStep component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.burnt.xiondemo.data.model.WalletState.Connecting copy(@org.jetbrains.annotations.NotNull()
        com.burnt.xiondemo.data.model.ConnectionStep step) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\n\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0013\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u00d6\u0003J\t\u0010\u0007\u001a\u00020\bH\u00d6\u0001J\t\u0010\t\u001a\u00020\nH\u00d6\u0001\u00a8\u0006\u000b"}, d2 = {"Lcom/burnt/xiondemo/data/model/WalletState$Disconnected;", "Lcom/burnt/xiondemo/data/model/WalletState;", "()V", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Disconnected extends com.burnt.xiondemo.data.model.WalletState {
        @org.jetbrains.annotations.NotNull()
        public static final com.burnt.xiondemo.data.model.WalletState.Disconnected INSTANCE = null;
        
        private Disconnected() {
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}