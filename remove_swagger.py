import os
import re

# 定义要处理的目录
controller_dir = "src/main/java/com/evaluate/controller"

# 获取所有Java文件
java_files = []
for root, dirs, files in os.walk(controller_dir):
    for file in files:
        if file.endswith(".java"):
            java_files.append(os.path.join(root, file))

print(f"Found {len(java_files)} Java files")

# Define replacement rules
replacements = [
    (r'import io\.swagger\.annotations\.Api;', '// import io.swagger.annotations.Api;'),
    (r'import io\.swagger\.annotations\.ApiOperation;', '// import io.swagger.annotations.ApiOperation;'),
    (r'import io\.swagger\.annotations\.ApiParam;', '// import io.swagger.annotations.ApiParam;'),
    (r'@Api\(tags = "[^"]*"\)', '// @Api(tags = "...")'),
    (r'@ApiOperation\("[^"]*"\)', '// @ApiOperation("...")'),
    (r'@ApiParam\("[^"]*"\)', '/* @ApiParam("...") */'),
]

# Process each file
for file_path in java_files:
    print(f"Processing file: {file_path}")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Apply replacement rules
    for pattern, replacement in replacements:
        content = re.sub(pattern, replacement, content)
    
    # Write back to file
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Completed processing: {file_path}")

print("All files processed successfully!")