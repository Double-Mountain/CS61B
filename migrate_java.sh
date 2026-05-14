#!/bin/bash

# 定义目标根目录
BASE_DIR="$(pwd)"
MAIN_JAVA_DIR="$BASE_DIR/src/main/java"
TEST_JAVA_DIR="$BASE_DIR/src/test/java"
POM_FILE="$BASE_DIR/pom.xml"

# 创建目标目录
mkdir -p "$MAIN_JAVA_DIR" "$TEST_JAVA_DIR"

# 处理 pom.xml（如果存在）
if [ -f "$POM_FILE" ]; then
    echo "发现 pom.xml，正在检查并移除冗余的 sourceDirectory 配置..."
    
    # 备份原文件
    cp "$POM_FILE" "$POM_FILE.bak"
    echo "已备份原文件为: $POM_FILE.bak"
    
    # 使用 sed 删除包含这两行的内容
    # 匹配并删除包含 <sourceDirectory>${project.basedir}</sourceDirectory> 的行
    # 以及包含 <testSourceDirectory>${project.basedir}</testSourceDirectory> 的行
    sed -i.bak -e '/<sourceDirectory>${project\.basedir}<\/sourceDirectory>/d' \
               -e '/<testSourceDirectory>${project\.basedir}<\/testSourceDirectory>/d' \
               "$POM_FILE"
    
    # 检查是否有改动
    if ! cmp -s "$POM_FILE.bak" "$POM_FILE"; then
        echo "已删除冗余的 sourceDirectory 配置行"
        rm -f "$POM_FILE.bak"  # 删除备份（因为 sed 已经创建了 .bak）
    else
        echo "未找到需要删除的配置行"
        rm -f "$POM_FILE.bak"
    fi
else
    echo "未发现 pom.xml，跳过 Maven 配置检查"
fi

# 移动所有 Java 文件
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
