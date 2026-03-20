#!/usr/bin/env bash
#
# build-mob-ios.sh
#
# Builds the mob Rust library for iOS targets, generates Swift bindings
# via UniFFI, and packages everything as an xcframework.
#
# Prerequisites:
#   - Xcode installed (with command line tools)
#   - Rust toolchain installed (rustup + cargo)
#   - Git
#
# Usage:
#   ./scripts/build-mob-ios.sh
#

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MOB_DIR="$PROJECT_ROOT/../third_party/mob"
FRAMEWORKS_DIR="$PROJECT_ROOT/XionDemo/Frameworks"
BINDINGS_OUTPUT_DIR="$PROJECT_ROOT/XionDemo/MobBindings"

# iOS targets
TARGETS=(
    "aarch64-apple-ios"
    "aarch64-apple-ios-sim"
    "x86_64-apple-ios"
)

# ---------------------------------------------------------------------------
# Step 1: Verify tools
# ---------------------------------------------------------------------------

echo "==> Checking environment..."

command -v rustup >/dev/null 2>&1 || { echo "ERROR: rustup not found. Install Rust: https://rustup.rs"; exit 1; }
command -v cargo  >/dev/null 2>&1 || { echo "ERROR: cargo not found. Install Rust: https://rustup.rs"; exit 1; }
command -v git    >/dev/null 2>&1 || { echo "ERROR: git not found."; exit 1; }
command -v xcodebuild >/dev/null 2>&1 || { echo "ERROR: xcodebuild not found. Install Xcode."; exit 1; }
command -v lipo   >/dev/null 2>&1 || { echo "ERROR: lipo not found."; exit 1; }

echo "    Rust toolchain = $(rustc --version)"
echo "    Xcode          = $(xcodebuild -version | head -1)"
echo ""

# ---------------------------------------------------------------------------
# Step 2: Clone mob repo if not already present
# ---------------------------------------------------------------------------

echo "==> Checking for mob source..."

if [ -d "$MOB_DIR" ]; then
    echo "    mob repo already exists at $MOB_DIR"
    echo "    Pulling latest changes..."
    git -C "$MOB_DIR" pull --ff-only || echo "    (pull skipped — may be on a pinned commit)"
else
    echo "    Cloning mob repo..."
    mkdir -p "$(dirname "$MOB_DIR")"
    git clone https://github.com/burnt-labs/mob "$MOB_DIR"
fi

echo ""

# ---------------------------------------------------------------------------
# Step 3: Install Rust iOS targets
# ---------------------------------------------------------------------------

echo "==> Installing Rust iOS targets..."

for TARGET in "${TARGETS[@]}"; do
    echo "    Adding target: $TARGET"
    rustup target add "$TARGET"
done

echo ""

# ---------------------------------------------------------------------------
# Step 4: Build mob for each iOS target
# ---------------------------------------------------------------------------

echo "==> Building mob for iOS targets..."

for TARGET in "${TARGETS[@]}"; do
    echo ""
    echo "--- Building for $TARGET ---"

    cargo build \
        --manifest-path "$MOB_DIR/Cargo.toml" \
        --release \
        --target "$TARGET"

    echo "    Build complete for $TARGET"
done

echo ""

# ---------------------------------------------------------------------------
# Step 5: Create universal simulator binary via lipo
# ---------------------------------------------------------------------------

echo "==> Creating universal simulator binary..."

SIM_ARM64="$MOB_DIR/target/aarch64-apple-ios-sim/release/libmob.a"
SIM_X86="$MOB_DIR/target/x86_64-apple-ios/release/libmob.a"
UNIVERSAL_SIM_DIR="$MOB_DIR/target/universal-sim/release"
UNIVERSAL_SIM="$UNIVERSAL_SIM_DIR/libmob.a"

mkdir -p "$UNIVERSAL_SIM_DIR"

if [ -f "$SIM_ARM64" ] && [ -f "$SIM_X86" ]; then
    lipo -create "$SIM_ARM64" "$SIM_X86" -output "$UNIVERSAL_SIM"
    echo "    Created universal simulator binary: $UNIVERSAL_SIM"
elif [ -f "$SIM_ARM64" ]; then
    cp "$SIM_ARM64" "$UNIVERSAL_SIM"
    echo "    Only arm64 sim binary available, using as-is"
else
    echo "    WARNING: No simulator binaries found"
fi

echo ""

# ---------------------------------------------------------------------------
# Step 6: Generate Swift bindings via UniFFI
# ---------------------------------------------------------------------------

echo "==> Generating Swift bindings..."

UNIFFI_BINDINGS=""
# Try to generate bindings using uniffi-bindgen
if cargo run --manifest-path "$MOB_DIR/Cargo.toml" --bin uniffi-bindgen -- \
    generate --library "$MOB_DIR/target/aarch64-apple-ios/release/libmob.a" \
    --language swift \
    --out-dir "$BINDINGS_OUTPUT_DIR" 2>/dev/null; then
    echo "    Generated Swift bindings in $BINDINGS_OUTPUT_DIR"
else
    echo "    WARNING: Could not auto-generate Swift bindings."
    echo "    You may need to generate them manually:"
    echo "      cargo run --bin uniffi-bindgen generate --library target/release/libmob.a --language swift --out-dir bindings/"
    echo "    Then copy the generated .swift file to: $BINDINGS_OUTPUT_DIR/"
fi

echo ""

# ---------------------------------------------------------------------------
# Step 7: Package as xcframework
# ---------------------------------------------------------------------------

echo "==> Creating xcframework..."

DEVICE_LIB="$MOB_DIR/target/aarch64-apple-ios/release/libmob.a"
XCFRAMEWORK_PATH="$FRAMEWORKS_DIR/libmob.xcframework"

# Remove old xcframework if it exists
rm -rf "$XCFRAMEWORK_PATH"
mkdir -p "$FRAMEWORKS_DIR"

if [ -f "$DEVICE_LIB" ] && [ -f "$UNIVERSAL_SIM" ]; then
    xcodebuild -create-xcframework \
        -library "$DEVICE_LIB" \
        -library "$UNIVERSAL_SIM" \
        -output "$XCFRAMEWORK_PATH"
    echo "    Created xcframework: $XCFRAMEWORK_PATH"
elif [ -f "$DEVICE_LIB" ]; then
    xcodebuild -create-xcframework \
        -library "$DEVICE_LIB" \
        -output "$XCFRAMEWORK_PATH"
    echo "    Created xcframework (device only): $XCFRAMEWORK_PATH"
else
    echo "    WARNING: Cannot create xcframework — no device library found"
fi

echo ""

# ---------------------------------------------------------------------------
# Done
# ---------------------------------------------------------------------------

echo "========================================="
echo "  Build complete!"
echo "========================================="
echo ""
echo "xcframework:  $XCFRAMEWORK_PATH"
echo "Bindings:     $BINDINGS_OUTPUT_DIR/"
echo ""
echo "Next steps:"
echo "  1. Open xion-ios.xcodeproj in Xcode"
echo "  2. Verify libmob.xcframework is in Frameworks (Do Not Embed — static)"
echo "  3. Add MobBindings/mob.swift to target sources"
echo "  4. Configure linker flags: -lresolv, Security.framework, SystemConfiguration.framework"
echo "  5. Build and run"
echo ""
