import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            original_content = content
            
            # 1. Update opacity to 94%
            content = content.replace('Color(0xFA040A18)', 'Color(0xF0040A18)')
            
            # 2. Remove redundant borders in HomeScreen
            if file == 'HomeScreen.kt':
                content = re.sub(r'\s*\.border\(\s*1\.dp\s*,\s*com\.example\.ui\.theme\.BorderColor\s*,\s*RoundedCornerShape\(12\.dp\)\s*\)', '', content)
                content = re.sub(r'\s*\.border\(\s*1\.dp\s*,\s*com\.example\.ui\.theme\.BorderColor\s*,\s*CircleShape\s*\)', '', content)

            if content != original_content:
                print(f'Updated {file}')
                with open(path, 'w') as f:
                    f.write(content)

