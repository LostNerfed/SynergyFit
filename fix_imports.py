import os

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

for root, _, files in os.walk(UI_DIR):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            if 'liquidGlassModifier' in content and 'import com.example.ui.theme.liquidGlassModifier' not in content and 'import com.example.ui.theme.*' not in content:
                print(f'Fixing imports in {file}')
                # Find the package line
                lines = content.split('\n')
                for i, line in enumerate(lines):
                    if line.startswith('import '):
                        lines.insert(i, 'import com.example.ui.theme.liquidGlassModifier')
                        break
                with open(path, 'w') as f:
                    f.write('\n'.join(lines))

