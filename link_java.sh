#!/bin/bash

# 定义目标根目录
BASE_DIR="$(pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"
TEST_JAVA_DIR="$BASE_DIR/src/test/java"
POM_FILE="$BASE_DIR/pom.xml"

# 创建 src/main 和 src/test 目录（如果不存在）
mkdir -p "$(dirname "$MAIN_JAVA_DIR")" "$(dirname "$TEST_JAVA_DIR")"

# 处理 src/main/java 符号链接
if [ -L "$MAIN_JAVA_DIR" ]; then
    echo "符号链接已存在: $MAIN_JAVA_DIR -> $(readlink "$MAIN_JAVA_DIR")"
elif [ -e "$MAIN_JAVA_DIR" ]; then
    echo "警告: $MAIN_JAVA_DIR 已存在且不是符号链接，跳过创建"
else
    ln -s "$BASE_DIR" "$MAIN_JAVA_DIR"
    echo "已创建符号链接: $MAIN_JAVA_DIR -> $BASE_DIR"
fi

# 处理 src/test/java 符号链接
if [ -L "$TEST_JAVA_DIR" ]; then
    echo "符号链接已存在: $TEST_JAVA_DIR -> $(readlink "$TEST_JAVA_DIR")"
elif [ -e "$TEST_JAVA_DIR" ]; then
    echo "警告: $TEST_JAVA_DIR 已存在且不是符号链接，跳过创建"
else
    ln -s "$BASE_DIR" "$TEST_JAVA_DIR"
    echo "已创建符号链接: $TEST_JAVA_DIR -> $BASE_DIR"
fi

# 处理 pom.xml（如果存在）
if [ -f "$POM_FILE" ]; then
    echo "发现 pom.xml，正在检查并移除冗余的 sourceDirectory 配置..."
    
    # 使用 sed 删除包含这两行的内容（忽略空白字符）
    sed -i -E -e '/<sourceDirectory>[[:space:]]*\$\{project\.basedir\}[[:space:]]*<\/sourceDirectory>/d' \
              -e '/<testSourceDirectory>[[:space:]]*\$\{project\.basedir\}[[:space:]]*<\/testSourceDirectory>/d' \
              "$POM_FILE"
    
    echo "已删除冗余的 sourceDirectory 配置行"
else
    echo "未发现 pom.xml，跳过 Maven 配置检查"
fi

echo ""
echo "操作完成！"
echo "现在的目录结构："
echo "  src/main/java -> . (指向当前目录)"
echo "  src/test/java -> . (指向当前目录)"
echo ""
echo "Maven 现在可以正确识别源代码和测试代码的位置了。"
echo "注意：无需移动任何文件，原有的 Java 文件位置保持不变。"
