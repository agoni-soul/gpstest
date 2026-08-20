ANDROID_BUILD_TOOLS="$HOME/Library/Android/sdk/build-tools/35.0.1"
D8="$ANDROID_BUILD_TOOLS/d8"
#JAVA_CLASSES="$HOME/Downloads/*.class"
OUTPUT_DEX="$HOME/Downloads"
SEARCH_DIR="$HOME/Downloads/"
#$HOME/Downloads/
#chmod +r $HOME/Downloads/*.class  # 添加读取权限
#CLASS_FILES=$(find "$SEARCH_DIR" -name "*.class" -print)
#
#if [ -z "$CLASS_FILES" ]; then
#    echo "❌ 错误：在 $SEARCH_DIR 中没有找到 .class 文件！"
#    exit 1
#fi

"$D8" --output $OUTPUT_DEX *.class

if [ -f "$OUTPUT_DEX/classes.dex" ]; then
    echo "✅ 成功生成 $SEARCH_DIR/classes.dex"
else
    echo "❌ 编译失败！"
    exit 1
fi