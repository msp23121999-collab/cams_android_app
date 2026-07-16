import os
import glob

directory = r"D:\cams-app-upload\app\src\main\java\com\example\features\parent\providers"

for filename in glob.glob(os.path.join(directory, "*.kt")):
    with open(filename, 'r') as f:
        content = f.read()
    
    # We want to replace the init block
    # from:
    #     init {
    #         loadData()
    #     }
    # to:
    #     init {
    #         viewModelScope.launch {
    #             repository.selectedChildId.collect { id ->
    #                 currentChildId = id
    #                 loadData()
    #             }
    #         }
    #     }
    
    new_init = """    init {
        viewModelScope.launch {
            repository.selectedChildId.collect { id ->
                currentChildId = id
                loadData()
            }
        }
    }"""
    
    # Simple replace
    old_init = """    init {
        loadData()
    }"""
    
    if old_init in content:
        content = content.replace(old_init, new_init)
        with open(filename, 'w') as f:
            f.write(content)
        print(f"Updated {filename}")
    else:
        print(f"init block not found in {filename}")

