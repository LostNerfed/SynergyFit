import os, re

UI_DIR = '/Users/erickc/.gemini/antigravity/scratch/SynergyFit/app/src/main/java/com/example/ui/screens'

# Fix HomeScreen.kt coach chat
path_home = os.path.join(UI_DIR, 'HomeScreen.kt')
with open(path_home, 'r') as f:
    home_content = f.read()

# 1. Modify the modifier of the OutlinedTextField in coach chat
home_content = re.sub(
    r'\.testTag\("coach_chat_input"\),',
    r'.testTag("coach_chat_input")\n                                .liquidGlassModifier(RoundedCornerShape(12.dp)),',
    home_content
)

# 2. Modify colors of the OutlinedTextField to be Transparent
home_content = re.sub(
    r'focusedBorderColor\s*=\s*androidx\.compose\.ui\.graphics\.Color\.White,',
    r'focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,',
    home_content
)
home_content = re.sub(
    r'unfocusedBorderColor\s*=\s*androidx\.compose\.ui\.graphics\.Color\.White,',
    r'unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,',
    home_content
)
home_content = re.sub(
    r'focusedContainerColor\s*=\s*Color\(0x05FFFFFF\),',
    r'focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,',
    home_content
)
home_content = re.sub(
    r'unfocusedContainerColor\s*=\s*Color\(0x05FFFFFF\)\),',
    r'unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent),',
    home_content
)

with open(path_home, 'w') as f:
    f.write(home_content)
print("Updated HomeScreen.kt coach input")

# Fix ActiveWorkoutScreen.kt weight/reps inputs
path_active = os.path.join(UI_DIR, 'ActiveWorkoutScreen.kt')
with open(path_active, 'r') as f:
    active_content = f.read()

# Replace the solid grey background and border with liquidGlassModifier
active_content = re.sub(
    r'\.background\(Color\(0xFF1E1E1E\),\s*RoundedCornerShape\(8\.dp\)\)\s*\n\s*\.border\(1\.dp,\s*BorderColor,\s*RoundedCornerShape\(8\.dp\)\)',
    r'.liquidGlassModifier(RoundedCornerShape(8.dp))',
    active_content
)

# Fix the manual exercise OutlinedTextField
active_content = re.sub(
    r'modifier\s*=\s*Modifier\.fillMaxWidth\(\),',
    r'modifier = Modifier.fillMaxWidth().liquidGlassModifier(RoundedCornerShape(12.dp)),',
    active_content
)
active_content = re.sub(
    r'focusedBorderColor\s*=\s*androidx\.compose\.ui\.graphics\.Color\.White,',
    r'focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,',
    active_content
)
active_content = re.sub(
    r'unfocusedBorderColor\s*=\s*androidx\.compose\.ui\.graphics\.Color\.White,',
    r'unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,',
    active_content
)

with open(path_active, 'w') as f:
    f.write(active_content)
print("Updated ActiveWorkoutScreen.kt inputs")

