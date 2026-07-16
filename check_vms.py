import os
import glob

vms = glob.glob("app/src/main/java/**/*ViewModel.kt", recursive=True)
for vm in vms:
    with open(vm, "r") as f:
        content = f.read()
    if "catch (e: Exception)" in content:
        # Check if every catch block contains isLoading = false
        import re
        catch_blocks = re.findall(r'catch\s*\([^\)]+\)\s*\{([^\}]+)\}', content)
        for i, block in enumerate(catch_blocks):
            if "isLoading = false" not in block and "isSaving = false" not in block and "finally" not in content:
                print(f"MISSING in {vm} block {i}: {block.strip()}")
