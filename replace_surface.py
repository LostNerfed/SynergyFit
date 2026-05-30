import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Replace remaining .background(AmoledSurface, shape) without borders
            content = re.sub(r'\.background\(\s*(?:com\.example\.ui\.theme\.)?AmoledSurface\s*,\s*([^)]+\))?\s*\)', lambda m: f'.liquidGlassModifier({m.group(1) or ""})', content)
            content = re.sub(r'\.background\(\s*(?:com\.example\.ui\.theme\.)?AmoledSurface\s*,\s*([A-Za-z0-9_]+)\s*\)', lambda m: f'.liquidGlassModifier({m.group(1)})', content)
            
            # Replace containerColor for sheets/dialogs
            content = re.sub(r'containerColor\s*=\s*(?:com\.example\.ui\.theme\.)?AmoledSurface', 'containerColor = Color(0x08FFFFFF)', content)
            
            # Replace TextField container colors
            content = re.sub(r'focusedContainerColor\s*=\s*(?:com\.example\.ui\.theme\.)?AmoledSurface', 'focusedContainerColor = Color(0x05FFFFFF)', content)
            content = re.sub(r'unfocusedContainerColor\s*=\s*(?:com\.example\.ui\.theme\.)?AmoledSurface', 'unfocusedContainerColor = Color(0x05FFFFFF)', content)

            with open(path, 'w') as f:
                f.write(content)

