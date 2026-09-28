import re

with open('C:\\Users\\samsung\\Desktop\\Android Project\\estor\\app\\src\\main\\java\\estor\\app\\PinActivity.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find checkFirstLaunch
idx = content.find('checkFirstLaunch')
if idx > 0:
    # Print 150 characters before and 600 after
    start = max(0, idx - 50)
    end = min(len(content), idx + 600)
    print("=== CONTEXT AROUND checkFirstLaunch ===")
    print(content[start:end])
    print("=== END ===")