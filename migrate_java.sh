#!/bin/bash

# 定义目标根目录
BASE_DIR="$(pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"
TEST_JAVA_DIR="$BASE_DIR/src/test/java"

# 创建目标目录（如果不存在）
mkdir -p "$MAIN_JAVA_DIR"
mkdir -p "$TEST_JAVA_DIR"

# 移动所有非测试的 .java 文件（不包含 "Test.java" 结尾的文件）
find "$BASE_DIR" -type f -name "*.java" ! -name "*Test.java" | while read -r file; do
    # 获取相对于当前路径的路径（去掉开头的 ./ 或 直接路径）
    rel_path="${file#$BASE_DIR/}"
    # 目标路径：src/main/java + 原相对路径所在目录
    dest_dir="$MAIN_JAVA_DIR/$(dirname "$rel_path")"
    mkdir -p "$dest_dir"
    mv "$file" "$dest_dir/"
    echo "已移动: $rel_path -> $dest_dir/"
done

# 移动所有 *Test.java 文件
find "$BASE_DIR" -type f -name "*Test.java" | while read -r file; do
    rel_path="${file#$BASE_DIR/}"
    dest_dir="$TEST_JAVA_DIR/$(dirname "$rel_path")"
    mkdir -p "$dest_dir"
    mv "$file" "$dest_dir/"
    echo "已移动: $rel_path -> $dest_dir/"
done

echo "操作完成。"
