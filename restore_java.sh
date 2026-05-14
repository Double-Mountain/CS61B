#!/bin/bash

# 定义源目录
BASE_DIR="$(pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"
TEST_JAVA_DIR="$BASE_DIR/src/test/java"

# 检查 src 目录是否存在
if [ ! -d "$BASE_DIR/src" ]; then
    echo "错误: 找不到 src 目录，当前目录可能不是迁移后的项目根目录"
    exit 1
fi

# 复制 main/java 中的所有文件回原位置
if [ -d "$MAIN_JAVA_DIR" ]; then
    find "$MAIN_JAVA_DIR" -type f -name "*.java" | while read -r file; do
        # 获取相对于 src/main/java 的路径
        rel_path="${file#$MAIN_JAVA_DIR/}"
        # 目标路径：当前目录 + 原相对路径
        dest="$BASE_DIR/$rel_path"
        # 确保目标目录存在
        mkdir -p "$(dirname "$dest")"
        cp "$file" "$dest"
        echo "已复制: $rel_path -> $dest"
    done
fi

# 复制 test/java 中的所有文件回原位置
if [ -d "$TEST_JAVA_DIR" ]; then
    find "$TEST_JAVA_DIR" -type f -name "*.java" | while read -r file; do
        rel_path="${file#$TEST_JAVA_DIR/}"
        dest="$BASE_DIR/$rel_path"
        mkdir -p "$(dirname "$dest")"
        cp "$file" "$dest"
        echo "已复制: $rel_path -> $dest"
    done
fi

# 确认是否删除 src 目录
echo ""
echo "文件已复制回原位置。"
read -p "是否删除整个 src 目录？(y/n): " confirm
if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
    rm -rf "$BASE_DIR/src"
    echo "已删除 src 目录及其所有内容。"
else
    echo "保留 src 目录。"
fi

echo "操作完成。"
