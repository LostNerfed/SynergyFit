import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Replace background
            new_content = re.sub(r'\.background\(\s*(?:com\.example\.ui\.theme\.)?AmoledBg\s*\)', '.background(Color.Transparent)', content)
            # Replace containerColor
            new_content = re.sub(r'containerColor\s*=\s*(?:com\.example\.ui\.theme\.)?AmoledBg', 'containerColor = Color.Transparent', new_content)
            
            if new_content != content:
                print(f'Updated {file}')
                with open(path, 'w') as f:
                    f.write(new_content)

