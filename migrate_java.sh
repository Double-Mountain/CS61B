#!/bin/bash

# 定义目标根目录
BASE_DIR="$(pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"
TEST_JAVA_DIR="$BASE_DIR/src/test/java"

# 创建目标目录
mkdir -p "$MAIN_JAVA_DIR" "$TEST_JAVA_DIR"

# 一次遍历处理所有文件
find "$BASE_DIR" -type f -name "*.java" | while read -r file; do
    filename=$(basename "$file")
    rel_path="${file#$BASE_DIR/}"
    
    # 判断是否为测试文件
    if [[ "$filename" == Test*.java ]] || [[ "$filename" == *Test.java ]]; then
        dest_dir="$TEST_JAVA_DIR/$(dirname "$rel_path")"
        echo "已移动 (test): $rel_path -> $dest_dir/"
    else
        dest_dir="$MAIN_JAVA_DIR/$(dirname "$rel_path")"
        echo "已移动 (main): $rel_path -> $dest_dir/"
    fi
    
    mkdir -p "$dest_dir"
    mv "$file" "$dest_dir/"
done

echo "操作完成。"
