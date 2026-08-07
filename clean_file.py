import re

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "r") as f:
    lines = f.readlines()

to_delete = []

# If we see `            }` or `                }` immediately followed by `        ).apply {`, it's bogus.
for i in range(len(lines)):
    if "}\n" in lines[i] and (lines[i].strip() == "}"):
        if i+1 < len(lines) and lines[i+1].startswith("        ).apply {"):
            to_delete.append(i)
        
        # also if it's just `        )` with a bogus `            }` before it
        elif i+1 < len(lines) and lines[i+1].strip() == ")" and i > 0:
            # Let's verify manually the ones we know
            pass

# The known bogus lines from grep are 367, 400, 538.
# wait, wait! In python 0-indexed: 366, 399, 537.
to_delete = []
for i in range(len(lines)):
    if "}\n" in lines[i] and lines[i].strip() == "}":
        if i+1 < len(lines) and lines[i+1].startswith("        ).apply {"):
            to_delete.append(i)

to_delete.sort(reverse=True)
for i in to_delete:
    del lines[i]

with open("app/src/main/java/com/noxscreen/app/BlackScreenService.kt", "w") as f:
    f.writelines(lines)
