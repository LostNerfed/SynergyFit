import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui'

# Regex to match:
# .background(AmoledSurface, Shape)
# .border(1.dp, PremiumGradientBorder, Shape)
# with optional com.example.ui.theme. prefixes and whitespace
pattern = re.compile(
    r'\.background\(\s*(?:com\.example\.ui\.theme\.)?AmoledSurface\s*,\s*([^)]+\))?\s*\)\s*\n?\s*\.border\(\s*(?:1|1\.5)\.dp\s*,\s*(?:com\.example\.ui\.theme\.)?PremiumGradientBorder\s*,\s*\1?\s*\)',
    re.MULTILINE
)

# Also handle cases where shape is just CircleShape (no parens)
pattern2 = re.compile(
    r'\.background\(\s*(?:com\.example\.ui\.theme\.)?AmoledSurface\s*,\s*([A-Za-z0-9_]+)\s*\)\s*\n?\s*\.border\(\s*(?:1|1\.5)\.dp\s*,\s*(?:com\.example\.ui\.theme\.)?PremiumGradientBorder\s*,\s*\1\s*\)',
    re.MULTILINE
)

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            new_content = pattern.sub(lambda m: f'.liquidGlassModifier({m.group(1) or ""})', content)
            new_content = pattern2.sub(lambda m: f'.liquidGlassModifier({m.group(1)})', new_content)
            
            if new_content != content:
                print(f'Updated {file}')
                with open(path, 'w') as f:
                    f.write(new_content)

