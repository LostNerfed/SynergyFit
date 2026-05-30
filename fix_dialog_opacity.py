import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Replace the almost transparent container color with a deeply opaque space blue
            new_content = content.replace('Color(0x08FFFFFF)', 'Color(0xFA040A18)')
            
            if new_content != content:
                print(f'Updated {file}')
                with open(path, 'w') as f:
                    f.write(new_content)

